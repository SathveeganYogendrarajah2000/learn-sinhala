package com.learnsinhala.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to inject the current authenticated user into controller methods.
 *
 * Usage:
 * @GetMapping("/me")
 * public User getCurrentUser(@CurrentUser UserDetails userDetails) {
 *     return userService.findByUsername(userDetails.getUsername());
 * }
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
