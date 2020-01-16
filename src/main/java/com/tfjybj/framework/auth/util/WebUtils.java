package com.tfjybj.framework.auth.util;


import com.dmsdbj.itoo.sso.utils.UserInfo;
import com.dmsdbj.itoo.sso.utils.UserUtil;
import com.tfjybj.framework.auth.util.evn.MacAddressHelper;
import com.tfjybj.framework.json.JsonHelper;
import com.tfjybj.framework.result.ApiResultWrapper;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * Http与Servlet工具类.
 */
public class WebUtils extends org.springframework.web.util.WebUtils {
    // -- Content Type 定义 --//
    public static final String TEXT_TYPE = "text/plain";
    public static final String JSON_TYPE = "application/json";
    public static final String XML_TYPE = "text/xml";
    public static final String HTML_TYPE = "text/html";
    public static final String JS_TYPE = "text/javascript";
    public static final String EXCEL_TYPE = "application/vnd.ms-excel";
    // -- Header 定义 --//
    public static final String AUTHENTICATION_HEADER = "Authorization";
    // -- 常用数值定义 --//
    public static final long ONE_YEAR_SECONDS = 60 * 60 * 24 * 365;
    private static final Log logger = LogFactory.getLog(WebUtils.class);
    // -- header 常量定义 --//
    private static final String HEADER_ENCODING = "encoding";
    private static final String HEADER_NOCACHE = "no-cache";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final boolean DEFAULT_NOCACHE = true;

    /**
     * 设置客户端缓存过期时间 Header.
     */
    public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
        // Http 1.0 header
        response.setDateHeader("Expires", System.currentTimeMillis() + expiresSeconds * 1000);
        // Http 1.1 header
        response.setHeader("Cache-Control", "max-age=" + expiresSeconds);
    }

    /**
     * 设置客户端无缓存Header.
     */
    public static void setNoCacheHeader(HttpServletResponse response) {
        // Http 1.0 header
        response.setDateHeader("Expires", 1L);
        response.addHeader("Pragma", "no-cache");
        // Http 1.1 header
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
    }

    /**
     * 设置LastModified Header.
     */
    public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedDate) {
        response.setDateHeader("Last-Modified", lastModifiedDate);
    }

    /**
     * 设置Etag Header.
     */
    public static void setEtag(HttpServletResponse response, String etag) {
        response.setHeader("ETag", etag);
    }

    /**
     * 根据浏览器If-Modified-Since Header, 计算文件是否已被修改.
     * <p/>
     * 如果无修改, checkIfModify返回false ,设置304 not modify status.
     *
     * @param lastModified 内容的最后修改时间.
     */
    public static boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response,
                                               long lastModified) {
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        if ((ifModifiedSince != -1) && (lastModified < ifModifiedSince + 1000)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return false;
        }
        return true;
    }

    /**
     * 根据浏览器 If-None-Match Header, 计算Etag是否已无效.
     * <p/>
     * 如果Etag有效, checkIfNoneMatch返回false, 设置304 not modify status.
     *
     * @param etag 内容的ETag.
     */
    public static boolean checkIfNoneMatchEtag(HttpServletRequest request, HttpServletResponse response, String etag) {
        String headerValue = request.getHeader("If-None-Match");
        if (headerValue != null) {
            boolean conditionSatisfied = false;
            if (!"*".equals(headerValue)) {
                StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");
                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(etag)) {
                        conditionSatisfied = true;
                    }
                }
            } else {
                conditionSatisfied = true;
            }
            if (conditionSatisfied) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.setHeader("ETag", etag);
                return false;
            }
        }
        return true;
    }

    /**
     * 检查浏览器客户端是否支持gzip编码.
     */
    public static boolean checkAccetptGzip(HttpServletRequest request) {
        // Http1.1 header
        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (StringUtils.contains(acceptEncoding, "gzip")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置Gzip Header并返回GZIPOutputStream.
     */
    public static OutputStream buildGzipOutputStream(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Encoding", "gzip");
        response.setHeader("Vary", "Accept-Encoding");
        return new GZIPOutputStream(response.getOutputStream());
    }

    /**
     * 设置让浏览器弹出下载对话框的Header.
     *
     * @param fileName 下载后的文件名.
     */
    public static void setDownloadableHeader(HttpServletResponse response, String fileName) {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    // 判断是否是IE
    public static boolean isIE(HttpServletRequest request) {
        return (getUserAgent(request).toLowerCase().indexOf("msie") > 0
                || getUserAgent(request).toLowerCase().indexOf("rv:11.0") > 0) ? true : false;
    }

    /**
     * 客户端信息
     *
     * @param request
     * @return
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("USER-AGENT");
    }

    /**
     * 设置让浏览器弹出下载对话框的Header.
     *
     * @param fileName 下载后的文件名.
     */
    public static void setDownloadableHeader(HttpServletRequest request, HttpServletResponse response,
                                             String fileName) {
        try {
            if (isIE(request)) {
                response.setHeader("content-disposition",
                        "attachment;filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
            } else {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "No-cache");
                response.setDateHeader("Expires", 0);
                // 中文文件名支持
                String encodedfileName = new String(fileName.getBytes(), "ISO8859-1");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
            }
        } catch (UnsupportedEncodingException e) {
        }
    }

    /**
     * 取得带相同前缀的Request Parameters.
     * <p/>
     * 返回的结果Parameter名已去除前缀.
     */
    public static Map<String, Object> getParametersStartingWith(HttpServletRequest request, String prefix) {
        return org.springframework.web.util.WebUtils.getParametersStartingWith(request, prefix);
    }

    /**
     * 判断请求是否是Ajax请求.
     *
     * @param request
     * @return
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        if (header != null && "XMLHttpRequest".equals(header)) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 对Http Basic验证的 Header进行编码.
     */
    // public static String encodeHttpBasic(String userName, String password) {
    // String encode = userName + ":" + password;
    // return "Basic " + EncodeUtils.base64Encode(encode.getBytes());
    // }

    /**
     * 取得HttpRequest中Parameter的简化方法.
     */
    public static <T> T getParameter(HttpServletRequest request, String name) {
        return (T) request.getParameter(name);
    }

    /**
     * 获取sessiont attribute
     *
     * @param name 属性名称
     * @return T
     */
    public static <T> T getSessionAttribute(HttpSession session, String name) {
        return (T) session.getAttribute(name);
    }

    /**
     * 分析并设置contentType与headers.
     */
    private static HttpServletResponse initResponseHeader(HttpServletResponse response, final String contentType,
                                                          final String... headers) {
        // 分析headers参数
        String encoding = DEFAULT_ENCODING;
        boolean noCache = DEFAULT_NOCACHE;
        for (String header : headers) {
            String headerName = org.apache.commons.lang3.StringUtils.substringBefore(header, ":");
            String headerValue = org.apache.commons.lang3.StringUtils.substringAfter(header, ":");
            if (StringUtils.equalsIgnoreCase(headerName, HEADER_ENCODING)) {
                encoding = headerValue;
            } else if (StringUtils.equalsIgnoreCase(headerName, HEADER_NOCACHE)) {
                noCache = Boolean.parseBoolean(headerValue);
            } else {
                throw new IllegalArgumentException(headerName + "不是一个合法的header类型");
            }
        }
        // 设置headers参数
        String fullContentType = contentType + ";charset=" + encoding;
        response.setContentType(fullContentType);
        if (noCache) {
            WebUtils.setNoCacheHeader(response);
        }
        return response;
    }
    /**
     * 直接输出内容的简便函数.
     *
     * eg. render("text/plain", "hello", "encoding:GBK"); render("text/plain", "hello",
     * "no-cache:false"); render("text/plain", "hello", "encoding:GBK", "no-cache:false");
     *
     * @param headers 可变的header数组，目前接受的值为"encoding:"或"no-cache:",默认值分别为UTF-8和true.
     */
    // public static void render(HttpServletResponse response,final String
    // contentType, final Object data, final String... headers) {
    // initResponseHeader(response,contentType, headers);
    // try {
    // JsonMapper.getInstance().writeValue(response.getWriter(),data);
    // } catch (IOException e) {
    // throw new RuntimeException(e.getMessage(), e);
    // }
    // }
    /**
     * 直接输出文本.
     *
     * @see #render(HttpServletResponse, String, Object, String...)
     * @param response
     * @param data 输出数据 可以是List Map等
     * @param headers 相应头 为null 则默认值：UTF-8编码 无缓存
     * @throws IOException
     */
    // public static void renderText(HttpServletResponse response,Object data, final String...
    // headers){
    // render(response,TEXT_TYPE,data,headers);
    // }
    /**
     * 直接输出HTML.
     *
     * @see #render(HttpServletResponse, String, Object, String...)
     * @param response
     * @param data 输出数据 可以是List Map等
     * @param headers 相应头 为null 则默认值：UTF-8编码 无缓存
     * @throws IOException
     */
    // public static void renderHtml(HttpServletResponse response,Object data,
    // final String... headers){
    // render(response,HTML_TYPE,data,headers);
    // }
    /**
     * 直接输出XML.
     *
     * @see #render(HttpServletResponse, String, Object, String...)
     * @param response
     * @param data 输出数据 可以是List Map等
     * @param headers 相应头 为null 则默认值：UTF-8编码 无缓存
     * @throws IOException
     */
    // public static void renderXml(HttpServletResponse response,Object data,
    // final String... headers){
    // renderXml(response,data,new XmlMapper(),headers);
    // }
    /**
     * 直接输出XML.
     *
     * @see #render(HttpServletResponse, String, Object, String...)
     * @param response
     * @param data 输出数据 可以是List Map等
     * @param xmlMapper {@link XmlMapper}
     * @param headers 相应头 为null 则默认值：UTF-8编码 无缓存
     * @throws IOException
     */
    // public static void renderXml(HttpServletResponse response,Object data,
    // XmlMapper xmlMapper,final String... headers){
    // try {
    // String result = xmlMapper.writeValueAsString(data);
    // render(response, XML_TYPE, result, headers);
    // } catch (IOException e) {
    // throw new RuntimeException(e.getMessage(), e);
    // }
    // }
    /**
     * 直接输出JSON.
     *
     * @see #render(HttpServletResponse, String, Object, String...)
     * @param response
     * @param data 输出数据 可以是List Map等
     * @param headers 相应头 为null 则默认值：UTF-8编码 无缓存
     * @throws IOException
     */
    // public static void renderJson(HttpServletResponse response,Object data,
    // final String... headers){
    // render(response,JSON_TYPE,data,headers);
    // }
    /**
     * 直接输出支持跨域Mashup的JSONP.
     *
     * @see #render(HttpServletResponse, String, Object, String...)
     * @param response
     * @param data 输出数据 可以是List Map等
     * @param headers 相应头 为null 则默认值：UTF-8编码 无缓存
     * @throws IOException
     */
    // public static void renderJsonp(final String callbackName,HttpServletResponse response,Object
    // data, final String... headers){
    // String result = JsonMapper.nonDefaultMapper().toJsonP(callbackName, data);
    // render(response,JSON_TYPE,result,headers);
    // }

    /**
     * 设置cookie.
     *
     * @param response
     * @param name
     * @param value
     * @param path
     */
    public static void setCookie(HttpServletResponse response, String name, String value, String path) {
        if (logger.isDebugEnabled()) {
            logger.debug("设置Cookie '" + name + "',位置: '" + path + "'");
        }
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(false);
        cookie.setPath(path);
        cookie.setMaxAge(2592000);
        response.addCookie(cookie);
    }

    /**
     * 获取Cookie.
     *
     * @param request
     * @param name
     * @return
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        Cookie returnCookie = null;
        if (cookies == null) {
            return returnCookie;
        }
        for (Cookie thisCookie : cookies) {
            if (thisCookie.getName().equals(name)) {
                if (!thisCookie.getValue().equals("")) {
                    returnCookie = thisCookie;
                    break;
                }
            }
        }
        return returnCookie;
    }

    /**
     * 删除Cookie.
     *
     * @param response
     * @param cookie
     * @param path
     */
    public static void deleteCookie(HttpServletResponse response, Cookie cookie, String path) {
        if (cookie != null) {
            cookie.setMaxAge(0);
            cookie.setPath(path);
            response.addCookie(cookie);
        }
    }

    public static String getAppURL(HttpServletRequest request) {
        StringBuffer url = new StringBuffer();
        int port = request.getServerPort();
        if (port < 0) {
            port = 80;
        }
        String scheme = request.getScheme();
        url.append(scheme);
        url.append("://");
        url.append(request.getServerName());
        if (((scheme.equals("http")) && (port != 80)) || ((scheme.equals("https")) && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        url.append(request.getContextPath());
        return url.toString();
    }

    /**
     * 允许 JS 跨域设置
     * <p>
     * <p>
     * <!-- 使用 nginx 注意在 nginx.conf 中配置 -->
     * <p>
     * http {
     * ......
     * add_header Access-Control-Allow-Origin *;
     * ......
     * }
     * </p>
     * <p>
     * <p>
     * 非 ngnix 下，如果该方法设置不管用、可以尝试增加下行代码。
     * <p>
     * response.setHeader("Access-Control-Allow-Origin", "*");
     * </p>
     *
     * @param response 响应请求
     */
    public static void allowJsCrossDomain(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS, POST, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * <p>
     * 判断请求是否为 AJAX
     * </p>
     *
     * @param request 当前请求
     */
    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ? true : false;
    }

    /**
     * <p>
     * AJAX 设置 response 返回状态
     * </p>
     *
     * @param status HTTP 状态码
     */
    public static void ajaxStatus(HttpServletResponse response, int status, String tip) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(status);
            PrintWriter out = response.getWriter();
            out.print(tip);
            out.flush();
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }

    /**
     * <p>
     * 获取当前 URL 包含查询条件
     * </p>
     *
     * @param encode URLEncoder编码格式
     */
    public static String getQueryString(HttpServletRequest request, String encode) throws IOException {
        StringBuffer sb = new StringBuffer(request.getRequestURL());
        String query = request.getQueryString();
        if (query != null && query.length() > 0) {
            sb.append("?").append(query);
        }
        return URLEncoder.encode(sb.toString(), encode);
    }

    /**
     * <p>
     * getRequestURL是否包含在URL之内
     * </p>
     *
     * @param url 参数为以';'分割的URL字符串
     */
    public static boolean inContainURL(HttpServletRequest request, String url) {
        boolean result = false;
        if (url != null && !"".equals(url.trim())) {
            String[] urlArr = url.split(";");
            StringBuffer reqUrl = new StringBuffer(request.getRequestURL());
            for (int i = 0; i < urlArr.length; i++) {
                if (reqUrl.indexOf(urlArr[i]) > 1) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * <p>
     * URLEncoder 返回地址
     * </p>
     *
     * @param url      跳转地址
     * @param retParam 返回地址参数名
     * @param retUrl   返回地址
     */
    public static String encodeRetURL(String url, String retParam, String retUrl) {
        return encodeRetURL(url, retParam, retUrl, null);
    }

    /**
     * <p>
     * URLEncoder 返回地址
     * </p>
     *
     * @param url      跳转地址
     * @param retParam 返回地址参数名
     * @param retUrl   返回地址
     * @param data     携带参数
     */
    public static String encodeRetURL(String url, String retParam, String retUrl, Map<String, String> data) {
        if (url == null) {
            return null;
        }
        StringBuffer retStr = new StringBuffer(url);
        retStr.append("?");
        retStr.append(retParam);
        retStr.append("=");
        try {
            retStr.append(URLEncoder.encode(retUrl, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encodeRetURL error." + url);
            e.printStackTrace();
        }
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                retStr.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return retStr.toString();
    }

    /**
     * <p>
     * URLDecoder 解码地址
     * </p>
     *
     * @param url 解码地址
     */
    public static String decodeURL(String url) {
        if (url == null) {
            return null;
        }
        String retUrl = "";
        try {
            retUrl = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("encodeRetURL error." + url);
            e.printStackTrace();
        }
        return retUrl;
    }

    /**
     * <p>
     * GET 请求
     * </p>
     *
     * @return boolean
     */
    public static boolean isGet(HttpServletRequest request) {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * POST 请求
     * </p>
     *
     * @return boolean
     */
    public static boolean isPost(HttpServletRequest request) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * 请求重定向至地址 location
     * </p>
     *
     * @param response 请求响应
     * @param location 重定向至地址
     */
    public static void sendRedirect(HttpServletResponse response, String location) {
        try {
            response.sendRedirect(location);
        } catch (IOException e) {
            logger.error("sendRedirect location:" + location);
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * 获取Request Playload 内容
     * </p>
     *
     * @return Request Playload 内容
     */
    public static String requestPlayload(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * <p>
     * 获取当前完整请求地址
     * </p>
     *
     * @return 请求地址
     */
    public static String getRequestUrl(HttpServletRequest request) {
        StringBuffer url = new StringBuffer(request.getScheme());
        // 请求协议 http,https
        url.append("://");
        url.append(request.getHeader("host"));// 请求服务器
        url.append(request.getRequestURI());// 工程名
        if (request.getQueryString() != null) {
            // 请求参数
            url.append("?").append(request.getQueryString());
        }
        return url.toString();
    }
    //    public static Cookie getCookie(HttpServletRequest request, String cookiename) {
    //        Cookie[] cookies = request.getCookies();
    //        if (cookies != null) {
    //            for (Cookie cookie : cookies) {
    //                if (cookiename.equals(cookie.getName())) {
    //                    return cookie;
    //                }
    //            }
    //        }
    //        return null;
    //    }

    /**
     * 获取 HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static HttpServletResponse getHttpServletResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    public static void writeJson(String json, HttpServletResponse response) {
        response.setContentType("application/json".intern());
        response.setCharacterEncoding("UTF-8".intern());
        try {
            response.getWriter().print(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String printResponse(HttpServletResponse response) throws Exception {
        Map requestMap = new HashMap<String, String>();
        requestMap.put("status".intern(), response.getStatus());
        return JsonHelper.toPrettyJson(requestMap);
    }

//    public static String printAuthRequest(HttpServletRequest request) throws Exception {
//        Map<String, String> headerMap = new HashMap<String, String>();
//        Enumeration headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String key = (String) headerNames.nextElement();
//            String value = request.getHeader(key);
//            headerMap.put(key, value);
//        }
//
//        Map requestMap = new HashMap<String, String>();
//        requestMap.put("uri".intern(), request.getRequestURI());
//        requestMap.put("method".intern(), request.getMethod());
//        requestMap.put("ip".intern(), MacAddressHelper.getIpAddr(request));
//        requestMap.put("headers:".intern(), headerMap);
//
//        return JsonHelper.toPrettyJson(requestMap);
//    }
//
//    public static String printRequest(HttpServletRequest request) throws Exception {
//        Map<String, String> headerMap = new HashMap<String, String>();
//        Enumeration headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String key = (String) headerNames.nextElement();
//            String value = request.getHeader(key);
//            headerMap.put(key, value);
//        }
//
//        Map requestMap = new HashMap<String, String>();
//        requestMap.put("uri".intern(), request.getRequestURI());
//        requestMap.put("method".intern(), request.getMethod());
////        requestMap.put("contextPath".intern(), request.getContextPath());
////        requestMap.put("authType".intern(), request.getAuthType());
////        requestMap.put("pathInfo".intern(), request.getPathInfo());
////        requestMap.put("queryString".intern(), request.getQueryString());
////        requestMap.put("remoteUser".intern(), request.getRemoteUser());
////        requestMap.put("localName".intern(), request.getLocalName());
////        requestMap.put("localAddr".intern(), request.getLocalAddr());
////        requestMap.put("localPort".intern(), request.getLocalPort());
////        requestMap.put("parts".intern(), JsonHelper.toPrettyJson(request.getParts()));
//        requestMap.put("ip".intern(), MacAddressHelper.getIpAddr(request));
//        requestMap.put("headers:".intern(), headerMap);
//
//        Map paramMap = request.getParameterMap();
//        if (paramMap.size() > 0) {
//            requestMap.put("params".intern(), paramMap.toString());//JsonHelper.toJson(paramMap));
//        }
//
////        try {
////            if (("POST".equals(request.getMethod().toUpperCase())
////                    || "PATCH".equals(request.getMethod().toUpperCase())
////                    || "DELETE".equals(request.getMethod().toUpperCase()))
////                    && request.getContentType().equalsIgnoreCase("application/json; charset=utf-8")) {
////                requestMap.put("Body", JsonHelper.convertJsonToMap(getRequestBody(request)));
////            }
////        }catch (Exception ex){
////            logger.error(ex.getMessage());
////        }
//
//        return JsonHelper.toPrettyJson(requestMap);
//    }

    public static String getRequestBody(HttpServletRequest request) {
        int size = request.getContentLength();
        if (size <= 0) {
            return "";
        }

        InputStream is = null;
        try {
            is = request.getInputStream();
            request.setCharacterEncoding("UTF-8".intern());
            byte[] reqBodyBytes = readBytes(is, size);
            String res = new String(reqBodyBytes);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return "";
    }


    /**
     * 使用PrintWriter流输出中文
     *
     * @param response
     * @throws IOException
     */
    public static String getResponseBody(HttpServletResponse response) {
        String data = "";
        PrintWriter out = null;//获取PrintWriter输出流
        try {
            out = response.getWriter();
            out.write(data);//使用PrintWriter流向客户端输出字符
        } catch (IOException e) {
            logger.warn(e.getMessage());
        } finally {
            IOUtils.closeQuietly(out);
        }
        return data;
    }

    public static Map<String, Object> getHttpInfo(HttpServletRequest request) {
        Map<String, Object> httpMap = new HashMap();

        if ("POST".equals(request.getMethod().toUpperCase())
                || "PATCH".equals(request.getMethod().toUpperCase())
                || "DELETE".equals(request.getMethod().toUpperCase())) {
            httpMap.put("params".intern(), ApiResultWrapper.getRequestBody());
        } else {
            if (!request.getRequestURI().contains("Authenticate".intern())) {
                Map<String, String[]> paramMap = request.getParameterMap();
                if (paramMap.size() > 0) {
                    Map<String, Object> tempMap = ApiResultWrapper.filterRequestParams(paramMap);
                    if (tempMap.size() > 0) {
                        httpMap.put("params".intern(), JsonHelper.toJson(tempMap).replace("\\\"", "\""));
                    }
                }
            }
        }

        Map<String, String> headerMap = new HashMap<String, String>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            headerMap.put(key, value);
        }
        httpMap.put("headers".intern(), headerMap);
//        httpMap.putAll(headerMap);

        try {
            httpMap.put("ip".intern(), MacAddressHelper.getIpAddr(request));
        } catch (Exception e) {
            logger.error("获取请求的ip失败");
        }

        httpMap.put("method".intern(), request.getMethod() + "_".intern() + request.getRequestURI());
        return httpMap;
    }

    public static Map<String, Object> getHttpInfo(HttpServletRequest request, Date requestTime, HttpServletResponse response, Date responseTime) {
        try {
            //request info
            Map<String, Object> httpMap = getHttpInfo(request);

//            "headers": {
//                "Access-Control-Expose-Headers": "X-Auth-Token",
//                        "connection": "keep-alive",
//                        "host": "localhost:8080",
//                        "accept-language": "zh-CN,zh;q=0.8,zh-TW;q=0.6",
//                        "accept": "*/*",
//                        "user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36",
//                        "Access-Control-Request-Method": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
//                        "x-auth-token": "bXl0BFcSDIVttrkqPTFpriztReOwVRuKjL+jViVtP4Myl2px9enE3ZDLjFkXpj0Ip47r9B+fpATZGdaZfug1Q0viklbULrfl",
//                        "cookie": "JSESSIONID=8100F2AE8C65871435D61CF560D4A258; token=bXl0BFcSDIVttrkqPTFpriztReOwVRuKjL+jViVtP4Myl2px9enE3ZDLjFkXpj0Ip47r9B+fpATZGdaZfug1Q0viklbULrfl",
//                        "Access-Control-Allow-Headers": "X-Auth-Token, Accept, Origin, X-Requested-With, Content-Type, Last-Modified",
//                        "Date": "Wed, 01 Mar 2017 02:10:22 GMT",
//                        "Transfer-Encoding": "chunked",
//                        "Access-Control-Allow-Origin": "*",
//                        "referer": "http://localhost:8080/app/version/",
//                        "accept-encoding": "gzip, deflate, sdch, br",
//                        "Content-Type": "application/json;charset=UTF-8"
//            },

//            Map<String, String> headerMap = (Map<String, String>) httpMap.get("headers");
//            Collection<String> resHeaderNames = response.getHeaderNames();
//            for (String key : resHeaderNames) {
//                String value = response.getHeader(key);
//                headerMap.put(key, value);
//            }
//            httpMap.put("headers".intern(), headerMap);

            httpMap.put("elapsed".intern(), responseTime.getTime() - requestTime.getTime());
            httpMap.put("status".intern(), response.getStatus());

            UserInfo curUser = UserUtil.getCurrentUser();
            if (curUser != null) {
                httpMap.put("user", JsonHelper.toJson(curUser));
            }

//            String message = "";
//            if (StringUtils.isEmpty(StringUtils.safeToString(httpMap.get("params")))) {
//                message = response.getStatus() + "_".intern() + request.getMethod() + "_".intern() + request.getRequestURI() + "_".intern()
//                        + StringUtils.safeToString(httpMap.get("elapsed"));
//            } else {
//                message = response.getStatus() + "_".intern() + request.getMethod() + "_".intern() + request.getRequestURI() + "_".intern() +
//                        StringUtils.safeToString(httpMap.get("params".intern())) + "_".intern() + StringUtils.safeToString(httpMap.get("elapsed"));
//            }
//            httpMap.put("message".intern(), message);
//            httpMap.put("access_type".intern(), "response".intern());

            return httpMap;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return new HashedMap();
    }

    public static final byte[] readBytes(InputStream is, int contentLen) throws IOException {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime = 0;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
                    if (readLengthThisTime == -1) {// Should not happen.
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return message;
            } catch (IOException e) {
                throw e;
            }
        }
        return new byte[]{};
    }
}
