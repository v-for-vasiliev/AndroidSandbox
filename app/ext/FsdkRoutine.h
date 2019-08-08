#include <iostream>
#include <fsdk/FaceEngine.h>
#include <vector>
#include <string>

#include <fsdk/Types/Image.h>

#include <android/log.h>

using namespace fsdk;

struct VectorArchive: fsdk::IArchive
{
	std::vector<uint8_t>& dataOut;
	int index = 0;

	bool write(const void* data, size_t size) noexcept override {
		const uint8_t* p = reinterpret_cast<const uint8_t*>(data);
		dataOut.insert(dataOut.end(), p, p+size);
		return true;
	}

	bool read(void* data, size_t size) noexcept override {
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
static IDescriptorExtractorPtr g_descriptorExtractor;
static IDescriptorMatcherPtr g_descriptorMatcher;

static fsdk::Ref<fsdk::IDetector> detector;

static IWarperPtr warper;							// Image warper


static IQualityEstimatorPtr qualityEstimator;
static ISmileEstimatorPtr smileEstimator;
static IGazeEstimatorPtr gazeEstimator;
static IEmotionsEstimatorPtr emotionsEstimator;
static IAttributeEstimatorPtr attributeEstimator;
static IHeadPoseEstimatorPtr headPoseEstimator;
static IGlassesEstimatorPtr glassesEstimator;
static IEyeEstimatorPtr eyeEstimator;

static fsdk::Image imageRGB;
static fsdk::Image warpedRGB;

bool isFrontendEdition()
{
	return g_engine->getFaceEngineEdition() == fsdk::FaceEngineEdition::FrontEndEdition;
}

static bool detect(fsdk::Detection &detection, fsdk::Landmarks68 &landmarks68, fsdk::Landmarks5 &landmarks5) {

	for (auto& pt : landmarks68.landmarks) {
		pt.x = 0;
		pt.y = 0;
	}

	for (auto& pt : landmarks5.landmarks) {
		pt.x = 0;
		pt.y = 0;
	}

	ResultValue<FSDKError, Face> result = detector->detectOne(imageRGB,imageRGB.getRect(),DetectionType::dtAll);

	if (result.isError())
	{
		__android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "No detections found. Reason: %s\n",result.what());
		return false;
	}

	detection = result.getValue().m_detection;
	landmarks68 = result.getValue().m_landmarks68.value();
	landmarks5 = result.getValue().m_landmarks5.value();

	return true;
}

static bool warpImage(const Image &image, Image &warp, fsdk::Detection &detection, fsdk::Landmarks5 landmarks, fsdk::Landmarks5 *transformedLandmarks = nullptr)
{
	const auto transformation = warper->createTransformation(detection, landmarks);
	const auto warpRes = warper->warp(image, transformation, warp);

	if (transformedLandmarks != nullptr)
	{
		warper->warp(landmarks, transformation, *transformedLandmarks);
	}

	return !warpRes.isError();
}


static int calcDetectionAndAttrs(float (&detectionRect)[4],
                                 fsdk::Landmarks68 &landmarks68,
                                 fsdk::EyesEstimation::EyeAttributes::EyelidLandmarks &eyelidLandmarksLeft,
                                 fsdk::EyesEstimation::EyeAttributes::EyelidLandmarks &eyelidLandmarksRight,
								 float (&ethnicity)[4],
                                 float (&headPose)[3],
                                 float (&attrEstimattion)[3],
                                 float &quality,
                                 float (&smile)[3],
                                 float (&emotionsEstimation)[7],
                                 int (&eyesStates)[2],
                                 float (&eyesGaze)[4]) {

	fsdk::Detection detection;
	fsdk::Landmarks5 landmarks5, transformedLandmarks;
    IWarperPtr warper = acquire(g_engine->createWarper());

	if (!detect(detection, landmarks68, landmarks5))
		return 1;

	detectionRect[0] = detection.rect.x;
	detectionRect[1] = detection.rect.y;
	detectionRect[2] = detection.rect.width;
	detectionRect[3] = detection.rect.height;

	const bool warpResult = warpImage(imageRGB, warpedRGB, detection, landmarks5, &transformedLandmarks);

    const fsdk::Transformation transformation = warper->createTransformation(detection, landmarks5);

	if (!warpResult)
	{
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Couldn't warp image\n");
		warpedRGB.reset();
		return 2;
	}

	Quality actual;

	auto result = qualityEstimator->estimate(warpedRGB, actual);

	if (result.isError()) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to estimate quality. Reason: %s", result.what());
		return 2;
	}

	quality = actual.getQuality();

	IAttributeEstimator::EstimationResult actualAttr;
	using Request = IAttributeEstimator::EstimationRequest;

    auto attrResult = attributeEstimator->estimate(warpedRGB, Request::estimateAll, actualAttr);

    if (attrResult.isError()) {
        __android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter",
                            "Failed to estimate attributes. Reason: %s", attrResult.what());
        return 2;
    }

    auto glassesResult = glassesEstimator->estimate(warpedRGB);
    if (glassesResult.isError()) {
        __android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter",
                            "Failed to estimate glasses. Reason: %s", glassesResult.what());
        return 2;
    }

    ethnicity[0] = actualAttr.ethnicity.value().africanAmerican;
    ethnicity[1] = actualAttr.ethnicity.value().indian;
    ethnicity[2] = actualAttr.ethnicity.value().asian;
    ethnicity[3] = actualAttr.ethnicity.value().caucasian;

    GlassesEstimation estimation = glassesResult.getValue();
    float glasses = 0.f;
    glasses = estimation == GlassesEstimation::NoGlasses ? 0.f : 1.f;

    attrEstimattion[0] = actualAttr.gender.value();
    attrEstimattion[1] = glasses;
    attrEstimattion[2] = actualAttr.age.value();

	HeadPoseEstimation headPoseEst;
	result = headPoseEstimator->estimate(imageRGB, detection, headPoseEst);

	if (result.isError()) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to estimate head pose. Reason: %s", result.what());
		return 2;
	}

	headPose[0] = headPoseEst.roll;
	headPose[1] = headPoseEst.pitch;
	headPose[2] = headPoseEst.yaw;

    EmotionsEstimation emotionsEst;

    result = emotionsEstimator->estimate(warpedRGB, emotionsEst);

    if (result.isError()) {
        __android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to estimate eyes. Reason: %s", result.what());
        return 2;
    }

    emotionsEstimation[0] = emotionsEst.anger;
    emotionsEstimation[1] = emotionsEst.disgust;
    emotionsEstimation[2] = emotionsEst.fear;
    emotionsEstimation[3] = emotionsEst.happiness;
    emotionsEstimation[4] = emotionsEst.neutral;
    emotionsEstimation[5] = emotionsEst.sadness;
    emotionsEstimation[6] = emotionsEst.surprise;

    //result = eyeEstimator->estimate(warpedRGB, transformedLandmarks, eyesEstimation);
    EyeCropper cropper;
    EyeCropper::EyesRects cropRoi;

    cropRoi = cropper.cropByLandmarks5(warpedRGB, transformedLandmarks);

    EyesEstimation eyesEstimation;
    result = eyeEstimator->estimate(warpedRGB, cropRoi, eyesEstimation);

    if (result.isError()) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to estimate eyes. Reason: %s", result.what());
		return 3;
	}

	eyesStates[0] = (int)eyesEstimation.leftEye.state;
	eyesStates[1] = (int)eyesEstimation.rightEye.state;

    SmileEstimation smileEst;
    result = smileEstimator->estimate(warpedRGB, smileEst);
    if (result.isError()) {
        __android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to estimate smile. Reason: %s", result.what());
        return 3;
    }

    smile[0] = smileEst.smile;
    smile[1] = smileEst.mouth;
    smile[2] = smileEst.occlusion;

    EyesEstimation EyesUnwarped;
    warper->unwarp(eyesEstimation, transformation, EyesUnwarped);

    eyelidLandmarksLeft  = EyesUnwarped.leftEye.eyelid;
    eyelidLandmarksRight = EyesUnwarped.rightEye.eyelid;

    GazeEstimation gazeEst;
    result = gazeEstimator->estimate(headPoseEst, EyesUnwarped, gazeEst);

    if (result.isError()) {
        __android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to estimate gaze. Reason: %s", result.what());
        return 3;
    }

    eyesGaze[0] = gazeEst.leftEye.yaw;
    eyesGaze[1] = gazeEst.leftEye.pitch;
    eyesGaze[2] = gazeEst.rightEye.yaw;
    eyesGaze[3] = gazeEst.rightEye.pitch;

	return 0;
}

static bool initFaceEngine(char const *dataPath)
{
    __android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "Load Face Engine data path: %s \n", dataPath);

    const std::string licenseConfPath = std::string{dataPath} + "/license.conf";

	g_engine = acquire(createFaceEngine(dataPath));
    fsdk::ILicense* license = g_engine->getLicense();

    if (!license) {
        __android_log_print(ANDROID_LOG_FATAL,"[INIT FACEENGINE]","failed to get license!");
        return false;
    }
    if (!fsdk::activateLicense(license, licenseConfPath.c_str())) {
        __android_log_print(ANDROID_LOG_FATAL,"[INIT FACEENGINE]","failed to activate license!");
        return false;
    }

	if(!g_engine) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create face engine instance.:  \n");
		return false;
	}

	if (!(detector = acquire(g_engine->createDetector()))) {

		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create detector (FACEDET_V1) instance.\n");
		return false;
	}

	if (!isFrontendEdition()) {
		if (!(g_descriptorExtractor = acquire(g_engine->createExtractor()))) {

			__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter",
								"Failed to create descriptor extractor instance.\n");
			return false;
		}

		if (!(g_descriptorMatcher = acquire(g_engine->createMatcher()))) {

			__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter",
								"Failed to create descriptor matcher instance.\n");
			return false;
		}
	}

	// Create CNN warper.
	if (!(warper = fsdk::acquire(g_engine->createWarper()))) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create face warper instance.");
		return false;
	}

	if (!(headPoseEstimator = fsdk::acquire(g_engine->createHeadPoseEstimator()))) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter",
							"Failed to create head pose estimator.");
		return false;
	}

	// Create estimator factory.
	if (!(attributeEstimator = fsdk::acquire(g_engine->createAttributeEstimator()))) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create attributes estimator.");
		return false;
	}

	// Create quality estimator.
	if (!(qualityEstimator = fsdk::acquire(g_engine->createQualityEstimator()))) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create image quality estimator.");
		return false;
	}

	// Create quality estimator.
	if (!(eyeEstimator = fsdk::acquire(g_engine->createEyeEstimator()))) {
		__android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create image eye estimator.");
		return false;
	}

    if (!(smileEstimator = fsdk::acquire(g_engine->createSmileEstimator()))) {
        __android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create image smile estimator.");
        return false;
    }

    if (!(gazeEstimator = fsdk::acquire(g_engine->createGazeEstimator()))) {
        __android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create image gaze estimator.");
        return false;
    }

    if (!(emotionsEstimator = fsdk::acquire(g_engine->createEmotionsEstimator()))) {
        __android_log_print(ANDROID_LOG_FATAL, "Fsdk adapter", "Failed to create emotions estimator.");
        return false;
    }

    if(!(glassesEstimator = fsdk::acquire(g_engine->createGlassesEstimator()))){
        __android_log_print(ANDROID_LOG_FATAL,"Fsdk adapter", "Failed to create ethinicty estimator. ");
        return false;
	}

    return true;
}

static IDescriptorPtr extractDescriptor()
{
	assert(g_descriptorExtractor);

    auto descriptor = acquire(g_engine->createDescriptor());

    __android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "Extracting descriptor...\n");

	if (!warpedRGB.isValid())
	{
		__android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "No warped image exists\n");
		return nullptr;
	}

    if (auto r = g_descriptorExtractor->extractFromWarpedImage(warpedRGB,  descriptor))
    {
        __android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "Descriptor extracted successfully.\n");
        return descriptor;
    }
    else
    {
        __android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "Failed to extract face descriptor. Reason:  %s \n", r.what());
    }

    return nullptr;
}

static float matchDescriptors(IDescriptorPtr first, IDescriptorPtr second) 
{
	float similarity(0.f);
	if (auto r = g_descriptorMatcher->match(first, second))
	{
		similarity = r.getValue().similarity;

		__android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "Derscriptors matched with score:  %f \n", similarity * 100.f);
	}
	else 
	{
		__android_log_print(ANDROID_LOG_ERROR, "Fsdk adapter", "Failed to match. Reason: %s ", r.what());
	}

	return similarity;
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
		//vlf::log::error("Failed to save face descriptor to vector.");
	}

	return data;
}

static void submitImage(unsigned char *buf, int width, int height)
{

	__android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "submitImage...");

	Image image;
	image.create(width, height, Format::R8G8B8X8, buf, true);

	if (!image.isValid())
	{
		__android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "Request image is invalid.\n");
		return;
	}

	image.convert(imageRGB, Format::R8G8B8);
}

fsdk::IDescriptorPtr getFaceDescriptor() {
    return extractDescriptor();
}

std::vector<uint8_t> getFaceDescriptorByteArray() {
	return faceDescriptorToByteArray( getFaceDescriptor() );
}

double matchDescriptorsArrays(std::vector<uint8_t> &descr1, std::vector<uint8_t> &descr2)
{
	fsdk::IDescriptorPtr descriptor1 = acquire(g_engine->createDescriptor());
	fsdk::IDescriptorPtr descriptor2 = acquire(g_engine->createDescriptor());

    if (descriptor1.get() == nullptr || descriptor2.get() == nullptr)
    {
        return 0.0f;
    }

	// load descriptor from vector uint8 data
	VectorArchive vectorArchive1(descr1), vectorArchive2(descr2);
	if (!descriptor1->load(&vectorArchive1)) {
	    return -1.0;
	}

	if (!descriptor2->load(&vectorArchive2)) {
        return -1.0;
    }

	double similarity = matchDescriptors(descriptor1, descriptor2);

	__android_log_print(ANDROID_LOG_INFO, "Fsdk adapter", "matchDescriptorsArrays similaritysimilarity: %f \n", similarity);

	return similarity;
}
