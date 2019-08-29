package ru.vasiliev.sandbox.sovestoauth.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Date: 06.07.2019
 *
 * @author Kirill Vasiliev
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface OAuthScope {

}
