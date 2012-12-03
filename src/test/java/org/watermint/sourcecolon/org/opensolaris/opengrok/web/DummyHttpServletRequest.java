/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * <p>
 * An dummy implementation of {@code HttpServletRequest} in which most methods
 * simply throw an exception. Unit tests that need an instance of
 * {@code HttpServletRequest} can create sub-classes that implement those
 * methods needed by the test case.
 * </p>
 * <p/>
 * <p>
 * Some methods that would have similar implementations in all sub-classes,
 * like set/get pairs, could be implemented here. Methods that would require
 * different implementations depending on the test, should rather just throw
 * an exception and let the tests override them.
 * </p>
 */
class DummyHttpServletRequest implements HttpServletRequest {

    private final Map<String, Object> attrs = new HashMap<>();

    @Override
    public String getAuthType() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Cookie[] getCookies() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public long getDateHeader(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getHeader(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public int getIntHeader(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getMethod() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getPathInfo() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getPathTranslated() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getContextPath() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getQueryString() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getRemoteUser() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean isUserInRole(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getRequestedSessionId() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getRequestURI() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getServletPath() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public HttpSession getSession(boolean b) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public HttpSession getSession() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public void logout() throws ServletException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Object getAttribute(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getCharacterEncoding() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public int getContentLength() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getContentType() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getParameter(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String[] getParameterValues(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getProtocol() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getScheme() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getServerName() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public int getServerPort() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getRemoteAddr() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getRemoteHost() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public void setAttribute(String s, Object o) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public void removeAttribute(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Locale getLocale() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean isSecure() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getRealPath(String s) {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public int getRemotePort() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getLocalName() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public String getLocalAddr() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public int getLocalPort() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public ServletContext getServletContext() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new IllegalStateException("No implementation");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new IllegalStateException("No implementation");
    }
}
