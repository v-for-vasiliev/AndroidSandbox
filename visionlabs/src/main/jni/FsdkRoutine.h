#include <iostream>
#include <fsdk/FaceEngine.h>
#include <lsdk/LivenessEngine.h>
#include <fsdk/IRefCounted.h>
#include <fsdk/Types/ResultValue.h>
#include <vector>
#include <string>
#include <stdarg.h>

using namespace fsdk;
using namespace lsdk;

#include <android/log.h>

#define LOG_INFO(TAG, ...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__);
#define LOG_WARN(TAG, ...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__);
#define LOG_FATAL(TAG, ...) __android_log_print(ANDROID_LOG_FATAL, TAG, __VA_ARGS__);


#include"Timer.h"

struct VectorArchive: fsdk::IArchive
{
    std::vector<uint8_t>& dataOut;
    int index = 0;

    bool write(const void* data, size_t size) noexcept override {
        const uint8_t* p = reinterpret_cast<const uint8_t*>(data);
        dataOut.insert(dataOut.end(), p, p+size);
        return true;
    }

    bool read(void* data, size_t size) noexcept  override {
        assert(size <= dataOut.size()-index);
        memcpy(data, (void*)&dataOut[0+index], size);
        index += size;
        return true;
    }

    void setSizeHint(size_t /*hint*/) noexcept override {}

    VectorArchive(std::vector<uint8_t>& inout):
            dataOut(inout)
    {}
};

static IFaceEnginePtr g_engine;
static ILivenessEnginePtr l_engine;
static IDescriptorExtractorPtr descriptorExtractor;
static IDescriptorMatcherPtr descriptorMatcher;
static IDetectorPtr detector;


static bool initFaceEngine(char const *dataPath)
{
    LOG_INFO("[INIT FACEENGINE]","Load Face Engine data path: %s \n", dataPath);
    g_engine = acquire(createFaceEngine(dataPath));

    const std::string licenseConfPath = std::string{dataPath} + "/license.conf";

    if(!g_engine) {
        LOG_FATAL("[INIT FACEENGINE]","Failed to create face engine instance.\n");
        return false;
    }

    fsdk::ILicense* license = g_engine->getLicense();

    if (!license) {
        LOG_FATAL("[INIT FACEENGINE]","failed to get license!");
        return false;
    }

    if (!fsdk::activateLicense(license, licenseConfPath.c_str())) {
        LOG_FATAL("[INIT FACEENGINE]","failed to activate license!");
        return false;
    }


    l_engine = acquire(createLivenessEngine(g_engine, dataPath));
    if(!l_engine){
        LOG_FATAL("Lsdk","Failed to create liveness engine instance.: \n");
        return false;
    }

    if(!(detector = acquire(g_engine->createDetector()))) {
        LOG_FATAL("[INIT FACEENGIE]","Failed to create detector.\n");
        return false;
    }

    if(g_engine->getFaceEngineEdition() != fsdk::FaceEngineEdition::CompleteEdition) {
        LOG_WARN("[INIT FACEENGINE]","FaceEngine SDK Frontend edition doesn't support face descriptors. Use FaceEngine SDK Complete edition\n");
    }

    if(g_engine->getFaceEngineEdition() == fsdk::FaceEngineEdition::CompleteEdition) {

        if (!(descriptorExtractor = acquire(g_engine->createExtractor()))) {
            LOG_INFO("Fsdk adapter", "Failed to create descriptorExtractor!\n");
            return false;
        }

        if (!(descriptorMatcher = acquire(g_engine->createMatcher()))) {
            LOG_INFO("Fsdk adapter","Failed to create descriptorMatcher!\n");
            return false;
        }
    }

    return true;
} // initFaceEngine


static IDescriptorPtr extractDescriptor(const Image& image)
{
    assert(descriptorExtractor);

    if (!image.isValid()) {
        LOG_INFO("Fsdk adapter", "Request image is invalid.\n");
        return nullptr;
    }

    Image imageRGB;
    image.convert(imageRGB, Format::R8G8B8);

    IDescriptorPtr descriptor=acquire(g_engine->createDescriptor());
    if(!descriptor){
        LOG_INFO("Fsdk adapter","Failed to create descriptor!");
        return nullptr;
    }
    LOG_INFO("Fsdk adapter", "Extracting descriptor.\n");

    if(auto r=descriptorExtractor->extractFromWarpedImage(imageRGB, descriptor)) {
        LOG_INFO("Fsdk adapter", "Descriptor extracted successfully.\n");
        return descriptor;
    }
    else {
        LOG_INFO("Fsdk adapter", "Failed to extract face descriptor. Reason:  %s \n", r.what());
        return nullptr;
    }
}

std::vector<uint8_t> faceDescriptorToByteArray(IDescriptorPtr descriptor)
{
    // Save face descriptor.
    std::vector<uint8_t> data;

    if (descriptor.get() == nullptr)
    {
        return data;
    }

    VectorArchive vectorArchive(data);
    if (!descriptor->save(&vectorArchive)) {
        LOG_INFO("Fsdk adapter", "Failed to save face descriptor to vector.");
    }

    return data;
}

fsdk::IDescriptorPtr getFaceDescriptorFromImageView(fsdk::Image viewRGBA)
{
    LOG_INFO("Fsdk adapter", "Extracting descriptor...");

    Image image;
    image.create(viewRGBA.getWidth(), viewRGBA.getHeight(), Format::R8G8B8X8, viewRGBA.getData(), true);

    return extractDescriptor(image);
}

static fsdk::Image imageRGB;

fsdk::Detection getFaceBbox(const fsdk::Image& image){
    //fsdk::Image rgbImage;
//
    //image.convert(rgbImage, fsdk::Format::R8G8B8);
//
    //auto error = detector->detectOne(rgbImage, rgbImage.getRect());
    auto error = detector->detectOne(imageRGB, imageRGB.getRect());
    if(error.isError()) {
        LOG_FATAL("Detector","Failed to detect face!\n");
        return fsdk::Detection{};
    }
    return error.getValue().m_detection;
}

std::vector<uint8_t> getFaceDescriptorByteArrayFromImageView(fsdk::Image viewRGBA)
{
    return faceDescriptorToByteArray( getFaceDescriptorFromImageView(viewRGBA) );
}

double matchDescriptorsArrays(std::vector<uint8_t> &descr1, std::vector<uint8_t> &descr2)
{
    IDescriptorPtr descriptor1 = acquire(g_engine->createDescriptor());
    IDescriptorPtr descriptor2 = acquire(g_engine->createDescriptor());
    float similarity = 0.f;

    // load descriptor from vector uint8 data
    VectorArchive vectorArchive1(descr1), vectorArchive2(descr2);

    if (!descriptor1->load(&vectorArchive1)) {
        LOG_FATAL("Fsdk adapter", "Failed to load data to descriptor info from vectorArchive1!");
        return -1.0;
    }

    if (!descriptor2->load(&vectorArchive2)) {
        LOG_FATAL("Fsdk adapter", "Failed to load data to descriptor info from vectorArchive2!");
        return -1.0;
    }

    fsdk::ResultValue<fsdk::FSDKError ,fsdk::MatchingResult> descriptorMatcherResult=descriptorMatcher->match(descriptor1,descriptor2);

    if(descriptorMatcherResult.isError()){
        LOG_FATAL("Fsdk adapter", "Failed to match! reason: %s \n",descriptorMatcherResult.what());
    }

    similarity=descriptorMatcherResult.getValue().similarity;

    LOG_INFO("Fsdk adapter", "DESCRIPTORS MATCHED WITH SCORE %f  \n", similarity);

    return similarity;
}

/*************************************************LIVENESS****************************************/


static EyeStates eyeState;
bool both_eyes_are_closed=false;
ILivenessPtr currentLiveness;
LivenessAlgorithmType chosenType;

// functions
static void submitImage(unsigned char *buf, int width, int height)
{
    LOG_INFO("LSDK", "width: %d   height: %d \n", width, height);

    Image image;
    image.create(width, height, Format::R8G8B8X8, buf, true);

    if (!image.isValid())
    {
        LOG_INFO("LSDK", "submitted image is invalid.\n");
        return;
    }
    image.convert(imageRGB, Format::R8G8B8);
}

static void setEyesToOpenState()
{
    eyeState.left  = 1;
    eyeState.right = 1;
}

static void setZoomLiveness()
{
    chosenType=LivenessAlgorithmType::LA_ZOOM;
    LOG_INFO("LivenessSDK","Choosen type is %d\n",chosenType);
}

static void setEyeLiveness()
{
    chosenType=LivenessAlgorithmType::LA_EYE;
    setEyesToOpenState();
    LOG_INFO("LivenessSDK","Choosen type is %d\n",chosenType);
}

static void startLivenessCheck()
{
    currentLiveness = fsdk::acquire(l_engine->createLiveness(chosenType));
    if(!currentLiveness){
        LOG_WARN("LivenessSDK","Unable to create Liveness with chosen Algorithm Type\n");
    }
}

static int getLivenessAction()
{
    LOG_INFO("LSDK", "returning chosen type is %d \n",(int)chosenType);
    return (int)chosenType;
}

static void resetLiveness()
{
    LOG_INFO("LSDK","Resetting liveness!");
    currentLiveness->reset();
}

static int checkLivenessCurrentStage()
{
    fsdk::ResultValue<LSDKError, bool> result = fsdk::makeResultValue(LSDKError::NotInitialized, false);

    {
        PROFILE_SCOPE("Update Time");
        result = currentLiveness->update(imageRGB);
    }

    switch(result.getError()) {
        case LSDKError::PreconditionFailed : return (int)LSDKError::PreconditionFailed;
        case LSDKError::NotReady : return (int)LSDKError::NotReady;
        case LSDKError::Ok : return result.getValue()? (int)LSDKError::Ok : -1;
        default : return (int)LSDKError::Internal;
    }

}