package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Voflix extends Spider {
    private static final String siteUrl = "https://www.voflix.com";
    private static final String siteHost = "www.voflix.com";
    static String cookie = "";

    /**
     * 播放源配置
     */
    private JSONObject playerConfig;
    /**
     * 筛选配置
     */
    private JSONObject filterConfig;
    private Pattern regexCategory = Pattern.compile("/type/(\\w+).html");
    private Pattern regexVid = Pattern.compile("/detail/(\\d+).html");
    private Pattern regexPlay = Pattern.compile("/play/(\\d+)-(\\d+)-(\\d+).html");
    private Pattern regexPage = Pattern.compile("/show/\\d+--------(\\d+)---.html");
    private Pattern userinfo = Pattern.compile("(?<=userinfo:\\s\').*?(?=\',)");
    private Pattern hostinfo = Pattern.compile("(?<=hostinfo:\\s\').*?(?=\',)");
    private Pattern challenge = Pattern.compile("(?<=challenge:\\s\').*?(?=\',)");
    private Pattern answer = Pattern.compile("(?<=answer:\\s\').*?(?=\',)");
    private Pattern fctoken = Pattern.compile("(?<=fc_token\":\").*?(?=\")");

//    protected String ext = null;

    @Override
    public void init(Context context) {
        super.init(context);
        try {
            playerConfig = new JSONObject("{\"duoduozy\":{\"show\":\"VOFLIX_国内\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"LINE406\":{\"show\":\"LINE406\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"LINE407\":{\"show\":\"LINE407\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"LINE408\":{\"show\":\"LINE408\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"p300\":{\"show\":\"LINE300\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"p301\":{\"show\":\"LINE301\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"line402-日语\":{\"show\":\"LINE402\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"LINE400\":{\"show\":\"LINE400\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"line401\":{\"show\":\"LINE401\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe268\":{\"show\":\"LINE268\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe290\":{\"show\":\"LINE290\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe291\":{\"show\":\"LINE291\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe296\":{\"show\":\"LINE296\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe297\":{\"show\":\"LINE297\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe307\":{\"show\":\"LINE307\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe308\":{\"show\":\"LINE308\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe309\":{\"show\":\"LINE309\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"line301\":{\"show\":\"LINE333\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"line302\":{\"show\":\"LINE302\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"LINE405\":{\"show\":\"LINE405\",\"des\":\"更多极速线路请访问APP\",\"ps\":\"0\",\"parse\":\"\"},\"LINE409\":{\"show\":\"LINE409\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"banquan\":{\"show\":\"已下架\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe261\":{\"show\":\"LINE261\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe265\":{\"show\":\"LINE265\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe278\":{\"show\":\"LINE278\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe306\":{\"show\":\"LINE306\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe317\":{\"show\":\"LINE317\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"xg_app_player\":{\"show\":\"app全局解析\",\"des\":\"\",\"ps\":\"1\",\"parse\":\"\"},\"iframe257\":{\"show\":\"LINE257\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe263\":{\"show\":\"LINE263\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe258\":{\"show\":\"LINE258\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe267\":{\"show\":\"LINE267\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe\":{\"show\":\"LINE200\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe262\":{\"show\":\"LINE262\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"iframe266\":{\"show\":\"LINE266\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"dplayer3\":{\"show\":\"播放线路3\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"dplayer2\":{\"show\":\"播放线路2\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"dplayer\":{\"show\":\"播放线路1\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"xunlei1\":{\"show\":\"百度云盘\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"if101国外\":{\"show\":\"VOFLIX_海外\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"https://demo.if101.tv/player/?url=\"},\"aliyun\":{\"show\":\"阿里云盘\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"xunlei2\":{\"show\":\"迅雷云盘\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"\":{\"show\":\"\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"LINE1080\":{\"show\":\"LINE1080\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"app\":{\"show\":\"LINEAPP\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"kuake\":{\"show\":\"夸克网盘\",\"des\":\"\",\"ps\":\"0\",\"parse\":\"\"}}");
//            playerConfig = new JSONObject("{\"g_app_player\":{\"show\":\"app全局解析\",\"or\":999,\"ps\":\"1\",\"parse\":\"\"},\"ks\":{\"show\":\"KS\",\"or\":999,\"ps\":\"1\",\"parse\":\"https://www.jubaibai.cc/jx/zl.html?url=\"},\"lm\":{\"show\":\"LM\",\"or\":999,\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"bs\":{\"show\":\"BS\",\"or\":999,\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"pq\":{\"show\":\"PQ\",\"or\":999,\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"jx\":{\"show\":\"JX\",\"or\":999,\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"vx\":{\"show\":\"v\",\"or\":999,\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"zl\":{\"show\":\"u64ad\",\"or\":999,\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"sm\":{\"show\":\"SM\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"https://player.movie09.com/dplayer/?url=\"},\"cl\":{\"show\":\"CL\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"dd\":{\"show\":\"DD\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"wx\":{\"show\":\"u5fae\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"/jx/index.php?url=\"},\"duoduozy\":{\"show\":\"4e9\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"https://play.shtpin.com/xplay/?url=\"},\"mjy\":{\"show\":\"MJ\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"lzm3u8\":{\"show\":\"LZ\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"https://player.movie09.com/dplayer/?url=\"},\"xlm3u8\":{\"show\":\"xl\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"https://player.movie09.com/dplayer/?url=\"},\"wjm3u8\":{\"show\":\"u4e4\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"https://player.movie09.com/dplayer/?url=\"},\"dbm3u8\":{\"show\":\"u5ea6\",\"or\":\"u652f\",\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"dplayer\":{\"show\":\"DP\",\"or\":\"dplayer.js.org\",\"ps\":\"0\",\"parse\":\"\"},\"videojs\":{\"show\":\"videojs-H5\",\"or\":\"videojs.com\",\"ps\":\"0\",\"parse\":\"\"},\"iframe\":{\"show\":\"iframe\",\"or\":\"iframe\",\"ps\":\"0\",\"parse\":\"\"},\"dm\":{\"show\":\"DM\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"kuake\":{\"show\":\"u5\",\"or\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"mjc\":{\"show\":\"MC(APP)\",\"or\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"rm\":{\"show\":\"RM(APP)\",\"or\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"wolong\":{\"show\":\"WL\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"/jx/zl.html?url=\"},\"yp\":{\"show\":\"u4e\",\"or\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"yz\":{\"show\":\"u6e9\",\"or\":\"\",\"ps\":\"0\",\"parse\":\"\"},\"zy\":{\"show\":\"u4e91\",\"or\":\"\",\"ps\":\"1\",\"parse\":\"http://116.62.17.106/copy.php?url=\"}}");
            filterConfig = new JSONObject("{\"dianshiju\":[{\"key\":\"area\",\"name\":\"地区\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"大陆\",\"v\":\"大陆\"},{\"n\":\"香港\",\"v\":\"香港\"},{\"n\":\"台湾\",\"v\":\"台湾\"},{\"n\":\"美国\",\"v\":\"美国\"},{\"n\":\"法国\",\"v\":\"法国\"},{\"n\":\"英国\",\"v\":\"英国\"},{\"n\":\"日本\",\"v\":\"日本\"},{\"n\":\"韩国\",\"v\":\"韩国\"},{\"n\":\"德国\",\"v\":\"德国\"},{\"n\":\"泰国\",\"v\":\"泰国\"},{\"n\":\"印度\",\"v\":\"印度\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"year\",\"name\":\"年份\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"2022\",\"v\":\"2022\"},{\"n\":\"2021\",\"v\":\"2021\"},{\"n\":\"2020\",\"v\":\"2020\"},{\"n\":\"2019\",\"v\":\"2019\"},{\"n\":\"2018\",\"v\":\"2018\"},{\"n\":\"2017\",\"v\":\"2017\"},{\"n\":\"2016\",\"v\":\"2016\"},{\"n\":\"2008\",\"v\":\"2008\"},{\"n\":\"2000\",\"v\":\"2000\"},{\"n\":\"1997\",\"v\":\"1997\"},{\"n\":\"1980\",\"v\":\"1980\"}]},{\"key\":\"by\",\"name\":\"排序\",\"value\":[{\"n\":\"时间\",\"v\":\"time\"},{\"n\":\"人气\",\"v\":\"hits\"},{\"n\":\"评分\",\"v\":\"score\"}]}]}");
        } catch (JSONException e) {
            SpiderDebug.log(e);
        }
    }


    protected static HashMap<String, String> Headers() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.62 Safari/537.36");

        return headers;
    }


    protected HashMap<String, String> getHeaders1(String url, String ref) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        if (ref.contains("ref")) {
            headers.put("Referer", ref.replace("ref:", ""));
        } else if (ref.contains("origin")) {
            headers.put("Origin", ref.replace("origin:", ""));
        }
        return headers;
    }

    /**
     * 爬虫headers
     * @return
     */
    protected HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("DNT", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        headers.put("Cookie", getCookie());
        return headers;
    }
    protected HashMap<String, String> getHeadersNoCookie() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("DNT", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        return headers;
    }

    private String getCookie() {
        if (!TextUtils.isEmpty(cookie)) {
            return cookie;
        }
        try {
            String Content = OkHttpUtil.string(siteUrl, getHeadersNoCookie());
            if (Content.contains("https://captcha.funcdn.com/jsCaptchaVerify")) {
                Matcher matcher = userinfo.matcher(Content);
                String usedata = "";
                if (matcher.find()) {
                    usedata = matcher.group(0);
                }

                Matcher matcher1 = hostinfo.matcher(Content);
                String hostdata = "";
                if (matcher1.find()) {
                    hostdata = matcher1.group(0);
                }

                Matcher matcher2 = challenge.matcher(Content);


                String challengedata = "";
                if (matcher2.find()) {
                    challengedata = matcher2.group(0);
                }
                Matcher matcher3 = answer.matcher(Content);

                String answerdata = "";
                if (matcher3.find()) {
                    answerdata = matcher3.group(0);
                }
                int code = 0;

                for (int i = 100000; i <= 999999; i++) {
                    String Md5 = Misc.MD5(challengedata + i, "UTF-8").toLowerCase();
                    if (Md5.contains(answerdata)) {
                        code = i;
                        break;

                    }
                }
                String body = "{\"userinfo\":" + "\"" + usedata + "\"," + "\"hostinfo\":" + "\"" + hostdata + "\"," + "\"challenge\":" + "\"" + challengedata + "\"," + "\"answer\":" + "\"" + answerdata + "\"," + "\"code\":" + "\"" + code + "\"" + "}";
                int cd = body.length();
                String cds = String.valueOf(cd);
                URL url = new URL("https://captcha.funcdn.com/jsCaptchaVerify");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "okhttp/3.12.11");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Length", cds);
                conn.setRequestProperty("Accept-Encoding", "gzip");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                String params = body;
                OutputStream out = conn.getOutputStream();
                out.write(params.getBytes());
                out.flush();
                out.close();
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                String htmls = "";
                while ((line = reader.readLine()) != null) {
                    htmls += line;
                }
                Matcher matcher4 = fctoken.matcher(htmls);

                if (matcher4.find()) {
                    cookie = "_funcdn_token=" + matcher4.group(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cookie;
    }

    /**
     * 获取分类数据 + 首页最近更新视频列表数据
     *
     * @param filter 是否开启筛选 关联的是 软件设置中 首页数据源里的筛选开关
     * @return
     */
    @Override
    public String homeContent(boolean filter) {
        try {
            String Content = OkHttpUtil.string(siteUrl, getHeaders());
            if (Content.contains("https://captcha.funcdn.com/jsCaptchaVerify")) {
                Matcher matcher = userinfo.matcher(Content);
                String usedata = "";
                if (matcher.find()) {
                    usedata = matcher.group(0);
                }

                Matcher matcher1 = hostinfo.matcher(Content);
                String hostdata = "";
                if (matcher1.find()) {
                    hostdata = matcher1.group(0);
                }

                Matcher matcher2 = challenge.matcher(Content);


                String challengedata = "";
                if (matcher2.find()) {
                    challengedata = matcher2.group(0);
                }
                Matcher matcher3 = answer.matcher(Content);

                String answerdata = "";
                if (matcher3.find()) {
                    answerdata = matcher3.group(0);
                }
                int code = 0;

                for (int i = 100000; i <= 999999; i++) {
                    String Md5 = Misc.MD5(challengedata + i, "UTF-8").toLowerCase();
                    if (Md5.contains(answerdata)) {
                        code = i;
                        break;

                    }
                }
                String body = "{\"userinfo\":" + "\"" + usedata + "\"," + "\"hostinfo\":" + "\"" + hostdata + "\"," + "\"challenge\":" + "\"" + challengedata + "\"," + "\"answer\":" + "\"" + answerdata + "\"," + "\"code\":" + "\"" + code + "\"" + "}";
                int cd = body.length();
                String cds = String.valueOf(cd);
                URL url = new URL("https://captcha.funcdn.com/jsCaptchaVerify");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "okhttp/3.12.11");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Length", cds);
                conn.setRequestProperty("Accept-Encoding", "gzip");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                String params = body;
                OutputStream out = conn.getOutputStream();
                out.write(params.getBytes());
                out.flush();
                out.close();
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                String htmls = "";
                while ((line = reader.readLine()) != null) {
                    htmls += line;
                }
                Matcher matcher4 = fctoken.matcher(htmls);

                if (matcher4.find()) {
                    cookie = "_funcdn_token=" + matcher4.group(0);
                }
            }

            Document doc = Jsoup.parse(OkHttpUtil.string(siteUrl, getHeaders()));
            // 分类节点
            Elements elements = doc.select("ul.navbar-items.swiper-wrapper > li a");
            System.out.println("cs" + elements);
            JSONArray classes = new JSONArray();
            for (Element ele : elements) {
                String name = ele.text();
                boolean show = name.equals("电影") ||
                        name.equals("剧集") ||
                        name.equals("动漫") ||
                        name.equals("综艺") ||
                        name.equals("国产剧") ||
                        name.equals("日韩剧") ||
                        name.equals("欧美剧");
                if (show) {
                    Matcher mather = regexCategory.matcher(ele.attr("href"));
                    if (!mather.find())
                        continue;
                    // 把分类的id和名称取出来加到列表里
                    String id = mather.group(1).trim();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type_id", id);
                    jsonObject.put("type_name", name);
                    classes.put(jsonObject);
                }
            }
            JSONObject result = new JSONObject();
            if (filter) {
                result.put("filters", filterConfig);
            }
            result.put("class", classes);
            try {
                // 取首页推荐视频列表
                Element homeList = doc.select("div.module-main.scroll-box").get(0);
                Elements list = homeList.select("div.module-items.module-poster-items-small.scroll-content a");
                JSONArray videos = new JSONArray();
                for (int i = 0; i < list.size(); i++) {
                    Element vod = list.get(i);
                    String title = vod.select("a").attr("title");
                    String cover = vod.select("img.lazy.lazyload").attr("data-original");
                    String remark = vod.select("div.module-item-note").text();
                    Matcher matcher = regexVid.matcher(vod.select("a").attr("href"));
                    if (!matcher.find())
                        continue;
                    String id = matcher.group(1);
                    JSONObject v = new JSONObject();
                    v.put("vod_id", id);
                    v.put("vod_name", title);
                    v.put("vod_pic", cover);
                    v.put("vod_remarks", remark);
                    videos.put(v);
                }
                result.put("list", videos);
            } catch (Exception e) {
                SpiderDebug.log(e);
            }
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    /**
     * 获取分类信息数据
     *
     * @param tid    分类id
     * @param pg     页数
     * @param filter 同homeContent方法中的filter
     * @param extend 筛选参数{k:v, k1:v1}
     * @return
     */
    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            String url = siteUrl + "/show/" + tid + "--------" + pg + "---" + ".html";
            System.out.println("ym" + url);
            String html = OkHttpUtil.string(url, getHeaders());
            Document doc = Jsoup.parse(html);
            JSONObject result = new JSONObject();
            int pageCount = 0;
            int page = -1;

            // 取页码相关信息
            Elements pageInfo = doc.select("div[id=page] a");
            if (pageInfo.size() == 0) {
                page = Integer.parseInt(pg);
                pageCount = page;
            } else {
                for (int i = 0; i < pageInfo.size(); i++) {
                    Element li = pageInfo.get(i);
                    Element a = li.selectFirst("a");
                    if (a == null)
                        continue;
                    String name = a.text();
                    if (page == -1 && li.hasClass("display")) {
                        Matcher matcher = regexPage.matcher(a.attr("href"));
                        if (matcher.find()) {
                            //  page = Integer.parseInt(matcher.group(1).split("-")[1]);
                            page = Integer.parseInt(matcher.group(1).trim());
                        } else {
                            page = 0;
                        }
                    }
                    if (name.equals("尾页")) {
                        Matcher matcher = regexPage.matcher(a.attr("href"));
                        if (matcher.find()) {
                            //    pageCount = Integer.parseInt(matcher.group(1).split("-")[1]);
                            pageCount = Integer.parseInt(matcher.group(1).trim());
                        } else {
                            pageCount = 0;
                        }
                        break;
                    }
                }
            }

            JSONArray videos = new JSONArray();
            if (!html.contains("没有找到您想要的结果哦")) {
                // 取当前分类页的视频列表
                Elements list = doc.select("div.module-items.module-poster-items-base a ");
                for (int i = 0; i < list.size(); i++) {
                    Element vod = list.get(i);
                    String title = vod.select("a").attr("title");
                    String cover = vod.select("img.lazy.lazyload").attr("data-original");
                    String remark = vod.select("div.module-item-note").text();
                    Matcher matcher = regexVid.matcher(vod.select("a").attr("href"));
                    if (!matcher.find())
                        continue;
                    String id = matcher.group(1);
                    JSONObject v = new JSONObject();
                    v.put("vod_id", id);
                    v.put("vod_name", title);
                    v.put("vod_pic", cover);
                    v.put("vod_remarks", remark);
                    videos.put(v);
                }
            }
            result.put("page", page);
            result.put("pagecount", pageCount);
            result.put("limit", 12);
            result.put("total", pageCount <= 1 ? videos.length() : pageCount * 12);

            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    private static String doReplaceRegex(Pattern pattern, String content) {
        if (pattern == null) {
            return content;
        }
        try {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return content;
    }

    /**
     * 视频详情信息
     *
     * @param ids 视频id
     * @return
     */
    @Override
    public String detailContent(List<String> ids) {
        try {
            // 视频详情url
            String url = siteUrl + "/detail/" + ids.get(0) + ".html";
            Document doc = Jsoup.parse(OkHttpUtil.string(url, getHeaders()));
            JSONObject result = new JSONObject();
            JSONObject vodList = new JSONObject();

            // 取基本数据
            String cover = doc.select(" div.module-item-pic  img").attr("data-original");
            String title = doc.select(" div.module-info-heading  h1").text();
            String category = "", area = "", year = "", remark = "", director = "", actor = "", desc = "";

            Elements span_text_muted = doc.select("div.module-info-items div.module-info-item  span");
            year = doc.selectFirst("div.module-info-tag > div > a").text();
            desc = doc.selectFirst("div.module-info-introduction-content p").text().trim();
            for (int i = 0; i < span_text_muted.size(); i++) {
                Element text = span_text_muted.get(i);
                String info = text.text();
                if (info.equals("更新：")) {
                    remark = text.nextElementSibling().text();
                    System.out.println("rma" + remark);
                } else if (info.equals("导演：")) {
                    List<String> directors = new ArrayList<>();
                    Elements aa = text.parent().select("div.module-info-item-content a");
                    for (int j = 0; j < aa.size(); j++) {
                        directors.add(aa.get(j).text());
                    }
                    director = TextUtils.join(",", directors);
                } else if (info.equals("主演：")) {
                    List<String> actors = new ArrayList<>();
                    Elements aa = text.parent().select("div.module-info-item-content a");
                    for (int j = 0; j < aa.size(); j++) {
                        actors.add(aa.get(j).text());
                    }
                    actor = TextUtils.join(",", actors);
                }
            }
            vodList.put("vod_id", ids.get(0));
            vodList.put("vod_name", title);
            vodList.put("vod_pic", cover);
            //   vodList.put("type_name", category);
            vodList.put("vod_year", year);
            //    vodList.put("vod_area", area);
            vodList.put("vod_remarks", remark);
            vodList.put("vod_actor", actor);
            vodList.put("vod_director", director);
            vodList.put("vod_content", desc);

            Map<String, String> vod_play = new LinkedHashMap<>();

            // 取播放列表数据
            Elements sources = doc.select("div[id='y-playList'] span");

            Elements sourceList = doc.select("div.module-play-list");

            for (int i = 0; i < sources.size(); i++) {
                Element source = sources.get(i);
                String sourceName = source.text();
                boolean found = false;
                for (Iterator<String> it = playerConfig.keys(); it.hasNext(); ) {
                    String flag = it.next();
                    if (playerConfig.getJSONObject(flag).getString("show").equals(sourceName)) {
                        sourceName = flag;
                        found = true;
                        break;
                    }
                }
                if (!found)
                    continue;
                String playList = "";
                Elements playListA = sourceList.get(i).select("a.module-play-list-link ");
                List<String> vodItems = new ArrayList<>();

                for (int j = 0; j < playListA.size(); j++) {
                    Element vod = playListA.get(j);
                    Matcher matcher = regexPlay.matcher(vod.attr("href"));

                    if (!matcher.find())
                        continue;
                    String playURL = matcher.group(1) + "-" + matcher.group(2) + "-" + matcher.group(3);
                    vodItems.add(vod.text() + "$" + playURL);
                }
                if (vodItems.size() > 0)
                    playList = TextUtils.join("#", vodItems);

                if (playList.length() == 0)
                    continue;
                vod_play.put(sourceName, playList);
            }

            if (vod_play.size() > 0) {
                String vod_play_from = TextUtils.join("$$$", vod_play.keySet());
                String vod_play_url = TextUtils.join("$$$", vod_play.values());
                vodList.put("vod_play_from", vod_play_from);
                vodList.put("vod_play_url", vod_play_url);
            }
            JSONArray list = new JSONArray();
            list.put(vodList);
            result.put("list", list);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }


    /**
     * 获取视频播放信息
     *
     * @param flag     播放源
     * @param id       视频id
     * @param vipFlags 所有可能需要vip解析的源
     * @return
     */
    private final Pattern urlt = Pattern.compile("\"url\": *\"([^\"]*)\",");
    private final Pattern token = Pattern.compile("\"token\": *\"([^\"]*)\"");
    private final Pattern vkey = Pattern.compile("\"vkey\": *\"([^\"]*)\",");
//    private final Pattern tm = Pattern.compile("\"tm\": *\"([^\"]*)\",");

    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            //定义播放用的headers

            String url = siteUrl + "/play/" + id + ".html";
            // JSONObject headers = new JSONObject();
            // headers.put("origin", " https://www.jubaibai.me/");
            // headers.put("User-Agent", " Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
            //  headers.put("Accept", " */*");
            //     headers.put("Accept-Language", " zh-CN,zh;q=0.9,en-US;q=0.3,en;q=0.7");
            //   headers.put("Accept-Encoding", " gzip, deflate");

            // 播放页 url
            Elements allScript = Jsoup.parse(OkHttpUtil.string(url, getHeaders())).select("script");
            JSONObject result = new JSONObject();
            for (int i = 0; i < allScript.size(); i++) {
                String scContent = allScript.get(i).html().trim();
                if (scContent.startsWith("var player_aaaa")) { // 取直链
                    int start = scContent.indexOf('{');
                    int end = scContent.lastIndexOf('}') + 1;

                    String json = scContent.substring(start, end);
                    JSONObject player = new JSONObject(json);

                    if (playerConfig.has(player.getString("from"))) {
                        JSONObject pCfg = playerConfig.getJSONObject(player.getString("from"));

                        String videoUrl = player.getString("url");
                        String playUrl = pCfg.getString("parse");
                        String show = pCfg.getString("show");
                        if (show.contains("VOFLIX")) {
                            String jxurl = "https://play.qwertwe.top/xplay/?url=" + videoUrl;

                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("referer", siteUrl);
                            Document doc = Jsoup.parse(OkHttpUtil.string(jxurl, headers));

                            Elements script = doc.select("body>script");
                            for (int j = 0; j < script.size(); j++) {
                                String content = script.get(j).html().trim();
                                if (content.contains("var config =")) {
                                    Matcher matcher1 = urlt.matcher(content);
                                    if (!matcher1.find())
                                        continue;
                                    Matcher matcher2 = token.matcher(content);
                                    if (!matcher2.find())
                                        continue;
                                    Matcher matcher3 = vkey.matcher(content);
                                    if (!matcher3.find())
                                        continue;
                                    String video_url = matcher1.group(1);
                                    String video_token = matcher2.group(1);
                                    String video_key = matcher3.group(1);
                                    String video_sign = "F4penExTGogdt6U8";
                                    String video_tm = String.valueOf(System.currentTimeMillis() / 1000);

                                    String xplayurl = "https://play.qwertwe.top/xplay/555tZ4pvzHE3BpiO838.php?tm=" + video_tm + "&url=" + video_url + "&vkey=" + video_key + "&token=" + video_token + "&sign=" + video_sign;
                                    String trueurls = OkHttpUtil.string(xplayurl, Headers());
                                    JSONObject trueurl = new JSONObject(trueurls);
                                    String urls = new String(Base64.decode(trueurl.getString("url").substring(8).getBytes(), Base64.DEFAULT));
                                    result.put("url", urls.substring(8, urls.length() - 8));

                                }
                            }
                            //    result.put("header", headers.toString());
                            result.put("parse", 0);
                            result.put("playUrl", "");
                        } else {
                            if (videoUrl.contains(".m3u8")) {
                                result.put("parse", 0);
                                result.put("playUrl", "");
                                result.put("url", videoUrl);
                                //   result.put("header", headers.toString());
                            }
                        }
                    }
                }
            }
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            long currentTime = System.currentTimeMillis();
            String url = siteUrl + "/index.php/ajax/suggest?mid=1&wd=" + URLEncoder.encode(key) + "&limit=10&timestamp=" + currentTime;
            JSONObject searchResult = new JSONObject(OkHttpUtil.string(url, getHeaders()));
            JSONObject result = new JSONObject();
            JSONArray videos = new JSONArray();
            if (searchResult.getInt("total") > 0) {
                JSONArray lists = new JSONArray(searchResult.getString("list"));
                for (int i = 0; i < lists.length(); i++) {
                    JSONObject vod = lists.getJSONObject(i);
                    String id = vod.getString("id");
                    String title = vod.getString("name");
                    String cover = vod.getString("pic");
                    JSONObject v = new JSONObject();
                    v.put("vod_id", id);
                    v.put("vod_name", title);
                    v.put("vod_pic", cover);
                    v.put("vod_remarks", "");
                    videos.put(v);
                }
            }
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }
}
