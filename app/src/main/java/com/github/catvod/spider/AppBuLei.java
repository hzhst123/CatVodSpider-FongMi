package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * M浏览器中的App影视
 * <p>
 * Author: 群友 不负此生
 */
public class AppBuLei extends Spider {

    private String apiUrl = "";
    private String key = "";

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        String strs[] = extend.split("\\$\\$\\$");
        String url = strs[0];
        if (strs.length > 1) {
            key = strs[1];
        }
        apiUrl = url;
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            String url = getCateUrl(apiUrl);
            JSONArray jsonArray = null;
            if (!url.isEmpty()) {
                SpiderDebug.log(url);
                String json = OkHttpUtil.string(url, getHeaders(url));
                JSONObject obj = new JSONObject(json);
                if (obj.has("list") && obj.get("list") instanceof JSONArray) {
                    jsonArray = obj.getJSONArray("list");
                } else if (obj.has("data") && obj.get("data") instanceof JSONObject && obj.getJSONObject("data").has("list") && obj.getJSONObject("data").get("list") instanceof JSONArray) {
                    jsonArray = obj.getJSONObject("data").getJSONArray("list");
                } else if (obj.has("data") && obj.get("data") instanceof JSONArray) {
                    jsonArray = obj.getJSONArray("data");
                }
            } else { // 通过filter列表读分类
                String filterStr = getFilterTypes(url, null);
                String[] classes = filterStr.split("\n")[0].split("\\+");
                jsonArray = new JSONArray();
                for (int i = 1; i < classes.length; i++) {
                    String[] kv = classes[i].trim().split("=");
                    if (kv.length < 2)
                        continue;
                    JSONObject newCls = new JSONObject();
                    newCls.put("type_name", kv[0].trim());
                    newCls.put("type_id", kv[1].trim());
                    jsonArray.put(newCls);
                }
            }
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    String typeName = jObj.getString("type_name");
                    if (isBan(typeName))
                        continue;
                    String typeId = jObj.getString("type_id");
                    JSONObject newCls = new JSONObject();
                    newCls.put("type_id", typeId);
                    newCls.put("type_name", typeName);
                    JSONObject typeExtend = jObj.optJSONObject("type_extend");
                    if (filter) {
                        String filterStr = getFilterTypes(url, typeExtend);
                        String[] filters = filterStr.split("\n");
                        JSONArray filterArr = new JSONArray();
                        for (int k = url.isEmpty() ? 1 : 0; k < filters.length; k++) {
                            String l = filters[k].trim();
                            if (l.isEmpty())
                                continue;
                            String[] oneLine = l.split("\\+");
                            String type = oneLine[0].trim();
                            String typeN = type;
                            if (type.contains("筛选")) {
                                type = type.replace("筛选", "");
                                if (type.equals("class"))
                                    typeN = "类型";
                                else if (type.equals("area"))
                                    typeN = "地区";
                                else if (type.equals("lang"))
                                    typeN = "语言";
                                else if (type.equals("year"))
                                    typeN = "年份";
                            }
                            JSONObject jOne = new JSONObject();
                            jOne.put("key", type);
                            jOne.put("name", typeN);
                            JSONArray valueArr = new JSONArray();
                            for (int j = 1; j < oneLine.length; j++) {
                                JSONObject kvo = new JSONObject();
                                String kv = oneLine[j].trim();
                                int sp = kv.indexOf("=");
                                if (sp == -1) {
                                    if (isBan(kv))
                                        continue;
                                    kvo.put("n", kv);
                                    kvo.put("v", kv);
                                } else {
                                    String n = kv.substring(0, sp);
                                    if (isBan(n))
                                        continue;
                                    kvo.put("n", n.trim());
                                    kvo.put("v", kv.substring(sp + 1).trim());
                                }
                                valueArr.put(kvo);
                            }
                            jOne.put("value", valueArr);
                            filterArr.put(jOne);
                        }
                        if (!result.has("filters")) {
                            result.put("filters", new JSONObject());
                        }
                        result.getJSONObject("filters").put(typeId, filterArr);
                    }
                    classes.put(newCls);
                }
            }
            result.put("class", classes);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String homeVideoContent() {
        try {
            String url = getRecommendUrl(apiUrl);
            boolean isTV = false;
            if (url.isEmpty()) {
                url = getCateFilterUrlPrefix(apiUrl) + "movie&page=1&area=&type=&start=";
                isTV = true;
            }
            SpiderDebug.log(url);
            String json = OkHttpUtil.string(url, getHeaders(url));
            JSONObject obj = new JSONObject(json);
            JSONArray videos = new JSONArray();
            if (isTV) {
                JSONArray jsonArray = obj.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject vObj = jsonArray.getJSONObject(i);
                    JSONObject v = new JSONObject();
                    v.put("vod_id", vObj.getString("nextlink"));
                    v.put("vod_name", vObj.getString("title"));
                    v.put("vod_pic", vObj.getString("pic"));
                    v.put("vod_remarks", vObj.getString("state"));
                    videos.put(v);
                }
            } else {
                ArrayList<JSONArray> arrays = new ArrayList<>();
                findJsonArray(obj, "vlist", arrays);
                if (arrays.isEmpty()) {
                    findJsonArray(obj, "vod_list", arrays);
                }
                List<String> ids = new ArrayList<>();
                for (JSONArray jsonArray : arrays) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject vObj = jsonArray.getJSONObject(i);
                        String vid = vObj.getString("vod_id");
                        if (ids.contains(vid))
                            continue;
                        ids.add(vid);
                        JSONObject v = new JSONObject();
                        v.put("vod_id", vid);
                        v.put("vod_name", vObj.getString("vod_name"));
                        v.put("vod_pic", vObj.getString("vod_pic"));
                        v.put("vod_remarks", vObj.getString("vod_remarks"));
                        videos.put(v);
                    }
                }
            }
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            String url = getCateFilterUrlPrefix(apiUrl) + tid + getCateFilterUrlSuffix(apiUrl);
            url = url.replace("#PN#", pg);
            url = url.replace("筛选class", (extend != null && extend.containsKey("class")) ? extend.get("class") : "");
            url = url.replace("筛选area", (extend != null && extend.containsKey("area")) ? extend.get("area") : "");
            url = url.replace("筛选lang", (extend != null && extend.containsKey("lang")) ? extend.get("lang") : "");
            url = url.replace("筛选year", (extend != null && extend.containsKey("year")) ? extend.get("year") : "");
            url = url.replace("排序", (extend != null && extend.containsKey("排序")) ? extend.get("排序") : "");
            SpiderDebug.log(url);
            String json = OkHttpUtil.string(url, getHeaders(url));
            JSONObject obj = new JSONObject(json);
            int totalPg = Integer.MAX_VALUE;
            try {
                if (obj.has("totalpage") && obj.get("totalpage") instanceof Integer) {
                    totalPg = obj.getInt("totalpage");
                } else if (obj.has("pagecount") && obj.get("pagecount") instanceof Integer) {
                    totalPg = obj.getInt("pagecount");
                } else if (obj.has("data") && obj.get("data") instanceof JSONObject &&
                        (obj.getJSONObject("data").has("total") && obj.getJSONObject("data").get("total") instanceof Integer &&
                                obj.getJSONObject("data").has("limit") && obj.getJSONObject("data").get("limit") instanceof Integer)) {
                    int limit = obj.getJSONObject("data").getInt("limit");
                    int total = obj.getJSONObject("data").getInt("total");
                    totalPg = total % limit == 0 ? (total / limit) : (total / limit + 1);
                }
            } catch (Exception e) {
                SpiderDebug.log(e);
            }

            JSONArray jsonArray = null;
            JSONArray videos = new JSONArray();
            if (obj.has("list") && obj.get("list") instanceof JSONArray) {
                jsonArray = obj.getJSONArray("list");
            } else if (obj.has("data") && obj.get("data") instanceof JSONObject && obj.getJSONObject("data").has("list") && obj.getJSONObject("data").get("list") instanceof JSONArray) {
                jsonArray = obj.getJSONObject("data").getJSONArray("list");
            } else if (obj.has("data") && obj.get("data") instanceof JSONArray) {
                jsonArray = obj.getJSONArray("data");
            }
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject vObj = jsonArray.getJSONObject(i);
                    if (vObj.has("vod_id")) {
                        JSONObject v = new JSONObject();
                        v.put("vod_id", vObj.getString("vod_id"));
                        v.put("vod_name", vObj.getString("vod_name"));
                        v.put("vod_pic", vObj.getString("vod_pic"));
                        v.put("vod_remarks", vObj.getString("vod_remarks"));
                        videos.put(v);
                    } else {
                        JSONObject v = new JSONObject();
                        v.put("vod_id", vObj.getString("nextlink"));
                        v.put("vod_name", vObj.getString("title"));
                        v.put("vod_pic", vObj.getString("pic"));
                        v.put("vod_remarks", vObj.getString("state"));
                        videos.put(v);
                    }
                }
            }
            JSONObject result = new JSONObject();
            result.put("page", pg);
            result.put("pagecount", totalPg);
            result.put("limit", 90);
            result.put("total", Integer.MAX_VALUE);
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            String key = buLeiApiKey();
            String time = String.valueOf((new Date()).getTime() / 1000L);
            String url = getPlayUrlPrefix(apiUrl) + ids.get(0) + "&apikey=" + key + "&keytime=" + time;
            SpiderDebug.log(url);
            String json = OkHttpUtil.string(url, getHeaders(url));
            JSONObject obj = new JSONObject(json);
            JSONObject result = new JSONObject();
            JSONObject vod = new JSONObject();
            genPlayList(apiUrl, obj, json, vod, ids.get(0));
            JSONArray list = new JSONArray();
            list.put(vod);
            result.put("list", list);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String bkey = buLeiApiKey();
            String time = String.valueOf((new Date()).getTime() / 1000L);

            String url = getSearchUrl(apiUrl, URLEncoder.encode(key)) + "&apikey=" + bkey + "&keytime=" + time;
            String json = OkHttpUtil.string(url, getHeaders(url));
            JSONObject obj = new JSONObject(json);
            JSONArray jsonArray = null;
            JSONArray videos = new JSONArray();
            if (obj.has("list") && obj.get("list") instanceof JSONArray) {
                jsonArray = obj.getJSONArray("list");
            } else if (obj.has("data") && obj.get("data") instanceof JSONObject && obj.getJSONObject("data").has("list") && obj.getJSONObject("data").get("list") instanceof JSONArray) {
                jsonArray = obj.getJSONObject("data").getJSONArray("list");
            } else if (obj.has("data") && obj.get("data") instanceof JSONArray) {
                jsonArray = obj.getJSONArray("data");
            }
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject vObj = jsonArray.getJSONObject(i);
                    if (vObj.has("vod_id")) {
                        JSONObject v = new JSONObject();
                        v.put("vod_id", vObj.getString("vod_id"));
                        v.put("vod_name", vObj.getString("vod_name"));
                        v.put("vod_pic", vObj.getString("vod_pic"));
                        v.put("vod_remarks", vObj.getString("vod_remarks"));
                        videos.put(v);
                    } else {
                        JSONObject v = new JSONObject();
                        v.put("vod_id", vObj.getString("nextlink"));
                        v.put("vod_name", vObj.getString("title"));
                        v.put("vod_pic", vObj.getString("pic"));
                        v.put("vod_remarks", vObj.getString("state"));
                        videos.put(v);
                    }
                }
            }
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            ArrayList<String> parseUrls = parseUrlMap.get(flag);
            if (parseUrls == null)
                parseUrls = new ArrayList<>();
            if (!parseUrls.isEmpty()) {
                JSONObject result = getFinalVideo(flag, parseUrls, id);
                if (result != null)
                    return result.toString();
            }
            if (Misc.isVideoFormat(id)) {
                JSONObject result = new JSONObject();
                result.put("parse", 0);
                result.put("playUrl", "");
                result.put("url", id);
                return result.toString();
            } else {
                JSONObject result = new JSONObject();
                result.put("parse", 1);
                result.put("jx", "1");
                result.put("url", id);
                return result.toString();
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    private void findJsonArray(JSONObject obj, String match, ArrayList<JSONArray> result) {
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            try {
                Object o = obj.get(k);
                if (k.equals(match) && o instanceof JSONArray)
                    result.add((JSONArray) o);
                if (o instanceof JSONObject) {
                    findJsonArray((JSONObject) o, match, result);
                } else if (o instanceof JSONArray) {
                    JSONArray array = (JSONArray) o;
                    for (int i = 0; i < array.length(); i++) {
                        findJsonArray(array.getJSONObject(i), match, result);
                    }
                }
            } catch (JSONException e) {
                SpiderDebug.log(e);
            }
        }
    }

    private String jsonArr2Str(JSONArray array) {
        try {
            ArrayList<String> strings = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                strings.add(array.getString(i));
            }
            return TextUtils.join(",", strings);
        } catch (JSONException e) {
        }
        return "";
    }

    private HashMap<String, String> getHeaders(String URL) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UA(URL));
        return headers;
    }

    private boolean isBan(String key) {
        return key.equals("伦理") || key.equals("情色") || key.equals("福利");
    }

    // M 扩展方法

    // ######重组搜索
    private String getSearchUrl(String URL, String KEY) {
        if (URL.contains(".vod")) {
            if (URL.contains("iopenyun.com")) {
                return URL + "/list?wd=" + KEY + "&page=";
            } else {
                return URL + "?wd=" + KEY + "&page=";
            }
        } else if (URL.contains("api.php/app") || URL.contains("xgapp")) {
            return URL + "search?text=" + KEY + "&pg=";
        } else if (urlPattern1.matcher(URL).find()) {
            if (URL.contains("esellauto")
                    || URL.contains("1.14.63.101")
                    || URL.contains("zjys")
                    || URL.contains("dcd")
                    || URL.contains("lxue")
                    || URL.contains("weetai.cn")
                    || URL.contains("haokanju1")
                    || URL.contains("fit:8")
                    || URL.contains("zjj.life")
                    || URL.contains("love9989")
                    || URL.contains("8d8q")
                    || URL.contains("lk.pxun")
                    || URL.contains("hgyx")
                    || URL.contains("521x5")
                    || URL.contains("lxyyy")
                    || URL.contains("0818tv")
                    || URL.contains("diyoui")
                    || URL.contains("diliktv")
                    || URL.contains("ppzhu")
                    || URL.contains("aitesucai")
                    || URL.contains("zz.ci")
                    || URL.contains("chxjon")
                    || URL.contains("watchmi")
                    || URL.contains("vipbp")
                    || URL.contains("bhtv")
                    || URL.contains("xfykl")) {
                return URL + "?ac=list&" + "wd=" + KEY + "&page=";
            } else {
                return URL + "?ac=list&" + "zm=" + KEY + "&page=";
            }
        }
        return "";
    }

    // ######UA
    private static final Pattern urlPattern1 = Pattern.compile("api\\.php/.*?/vod");
    private static final Pattern urlPattern2 = Pattern.compile("api\\.php/.+?\\.vod");
    private static final Pattern parsePattern = Pattern.compile("/.+\\?.+=");
    private static final Pattern parsePattern1 = Pattern.compile(".*(url|v|vid|php\\?id)=");
    private static final Pattern parsePattern2 = Pattern.compile("https?://[^/]*");

    protected static final Pattern[] htmlVideoKeyMatch = new Pattern[]{
            Pattern.compile("player=new"),
            Pattern.compile("<div id=\"video\""),
            Pattern.compile("<div id=\"[^\"]*?player\""),
            Pattern.compile("//视频链接"),
            Pattern.compile("HlsJsPlayer\\("),
            Pattern.compile("<iframe[\\s\\S]*?src=\"[^\"]+?\""),
            Pattern.compile("<video[\\s\\S]*?src=\"[^\"]+?\"")
    };

    private String UA(String URL) {
        if (URL.contains("vod.9e03.com")) {
            return "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Mobile Safari/537.36";
        } else if (URL.contains("api.php/app") || URL.contains("xgapp") || URL.contains("freekan")) {
            return "Dart/2.14 (dart:io)";
        } else if (URL.contains("zsb") || URL.contains("fkxs") || URL.contains("xays") || URL.contains("xcys") || URL.contains("szys") || URL.contains("dxys") || URL.contains("ytys") || URL.contains("qnys")) {
            return "Dart/2.15 (dart:io)";
        } else if (URL.contains(".vod")) {
            return "okhttp/4.1.0";
        } else {
            return "Dalvik/2.1.0";
        }
    }

    // ######获取分类地址
    String getCateUrl(String URL) {
        if (URL.contains("api.php/app") || URL.contains("xgapp")) {
            return URL + "nav?token=";
        } else if (URL.contains(".vod")) {
            if (URL.contains("iopenyun.com")) {
                return URL + "/list?type";
            } else {
                return URL + "/types";
            }
        } else {
            return "";
        }
    }

    // ######分类筛选前缀地址
    String getCateFilterUrlPrefix(String URL) {
        if (URL.contains("api.php/app") || URL.contains("xgapp")) {
            if (URL.contains("dijiaxia")) {
                URL = "http://www.dijiaxia.com/api.php/app/";
                return URL + "video?tid=";
            } else {
                return URL + "video?tid=";
            }
        } else if (URL.contains(".vod")) {
            if (URL.contains("iopenyun")) {
                return URL + "/list?type=";
            } else {
                return URL + "?type=";
            }
        } else {
            return URL + "?ac=list&class=";
        }
    }

    // ######分类筛选后缀地址
    String getCateFilterUrlSuffix(String URL) {
        if (URL.contains("api.php/app") || URL.contains("xgapp")) {
            return "&class=筛选class&area=筛选area&lang=筛选lang&year=筛选year&limit=18&pg=#PN#";
        } else if (URL.contains(".vod")) {
            return "&class=筛选class&area=筛选area&lang=筛选lang&year=筛选year&by=排序&limit=18&page=#PN#";
        } else {
            return "&page=#PN#&area=筛选area&type=筛选class&start=筛选year";
        }
    }

    // ######筛选内容
    String getFilterTypes(String URL, JSONObject typeExtend) {
        String str = "";
        if (typeExtend != null) {
            Iterator<String> typeExtendKeys = typeExtend.keys();
            while (typeExtendKeys.hasNext()) {
                String key = typeExtendKeys.next();
                if (key.equals("class") || key.equals("area") || key.equals("lang") || key.equals("year")) {
                    try {
                        str = str + "筛选" + key + "+全部=+" + typeExtend.getString(key).replace(",", "+") + "\n";
                    } catch (JSONException e) {
                    }
                }
            }
        }
        if (URL.contains(".vod")) {
            str += "\n" + "排序+全部=+最新=time+最热=hits+评分=score";
        } else if (URL.contains("api.php/app") || URL.contains("xgapp")) {
        } else {
            str = "分类+全部=+电影=movie+连续剧=tvplay+综艺=tvshow+动漫=comic+4K=movie_4k+体育=tiyu\n筛选class+全部=+喜剧+爱情+恐怖+动作+科幻+剧情+战争+警匪+犯罪+动画+奇幻+武侠+冒险+枪战+恐怖+悬疑+惊悚+经典+青春+文艺+微电影+古装+历史+运动+农村+惊悚+惊悚+伦理+情色+福利+三级+儿童+网络电影\n筛选area+全部=+大陆+香港+台湾+美国+英国+法国+日本+韩国+德国+泰国+印度+西班牙+加拿大+其他\n筛选year+全部=+2022+2021+2020+2019+2018+2017+2016+2015+2014+2013+2012+2011+2010+2009+2008+2007+2006+2005+2004+2003+2002+2001+2000";
        }
        return str;
    }

    // ######推荐地址
    String getRecommendUrl(String URL) {
        if (URL.contains("api.php/app") || URL.contains("xgapp")) {
            return URL + "index_video?token=";
        } else if (URL.contains(".vod")) {
            return URL + "/vodPhbAll";
        } else {
            return "";
        }
    }

    // ######播放器前缀地址
    String getPlayUrlPrefix(String URL) {
        if (URL.contains("api.php/app") || URL.contains("xgapp")) {
            if (URL.contains("dijiaxia")) {
                URL = "https://www.dijiaxia.com/api.php/app/";
                return URL + "video_detail?id=";
            } else if (URL.contains("1010dy")) {
                URL = "http://www.1010dy.cc/api.php/app/";
                return URL + "video_detail?id=";
            } else {
                return URL + "video_detail?id=";
            }
        } else if (URL.contains(".vod")) {
            if (URL.contains("iopenyun")) {
                return URL + "/detailID?vod_id=";
            } else {
                return URL + "/detail?vod_id=";
            }
        } else {
            return "";
        }
    }

    // ######选集
    protected final HashMap<String, ArrayList<String>> parseUrlMap = new HashMap<>();

    private void genPlayList(String URL, JSONObject object, String json, JSONObject vod, String vid) throws JSONException {
        ArrayList<String> playUrls = new ArrayList<>();
        ArrayList<String> playFlags = new ArrayList<>();
        JSONObject data = object.getJSONObject("data");
        vod.put("vod_id", data.optString("vod_id", vid));
        vod.put("vod_name", data.getString("vod_name"));
        vod.put("vod_pic", data.getString("vod_pic"));
        vod.put("type_name", data.optString("vod_class"));
        vod.put("vod_year", data.optString("vod_year"));
        vod.put("vod_area", data.optString("vod_area"));
        vod.put("vod_remarks", data.optString("vod_remarks"));
        vod.put("vod_actor", data.optString("vod_actor"));
        vod.put("vod_director", data.optString("vod_director"));
        vod.put("vod_content", data.optString("vod_content"));
        JSONArray vodUrlWithPlayer = data.getJSONArray("vod_play_list");
        for (int i = 0; i < vodUrlWithPlayer.length(); i++) {
            JSONObject from = vodUrlWithPlayer.getJSONObject(i);
            String flag = from.getJSONObject("player_info").optString("from").trim();
            if (flag.isEmpty())
                flag = from.getJSONObject("player_info").optString("show").trim();
            playFlags.add(flag);

            JSONArray urls = from.getJSONArray("urls");
            ArrayList<String> urlList = new ArrayList<>();
            for (int k = 0; k < urls.length(); k++) {
                String url = decryptRC4(urls.getJSONObject(k).getString("url").substring(6), getDecryptKey());
                urlList.add(urls.getJSONObject(k).getString("name") + "$" + url);
            }
            playUrls.add(TextUtils.join("#", urlList));
            try {
                ArrayList<String> parses = new ArrayList<>();
                String parseStr1 = from.getJSONObject("player_info").optString("parse");
                String parseStr2 = from.getJSONObject("player_info").optString("parse2");
                if (parseStr1.length() > 6) {
                    parseStr1 = decryptRC4(parseStr1.substring(6), getDecryptKey());
                    parses.addAll(Arrays.asList(parseStr1.split(",")));
                }
                if (parseStr2.length() > 6) {
                    parseStr2 = decryptRC4(parseStr2.substring(6), getDecryptKey());
                    parses.addAll(Arrays.asList(parseStr2.split(",")));
                }

                ArrayList<String> parseUrls = parseUrlMap.get(flag);
                if (parseUrls == null) {
                    parseUrls = new ArrayList<>();
                    parseUrlMap.put(flag, parseUrls);
                }
                for (String purl : parses) {

                    if (purl.contains("http")) {
                        Matcher matcher = parsePattern1.matcher(purl);
                        if (matcher.find()) {
                            purl = matcher.group(0);
                        }
                    } else if (purl.contains("//")) {
                        Matcher matcher = parsePattern1.matcher(purl);
                        if (matcher.find()) {
                            purl = "http:" + matcher.group(0);
                        }
                    } else {
                        Matcher matcher = parsePattern2.matcher(URL);
                        if (matcher.find()) {
                            Matcher matcher1 = parsePattern1.matcher(URL);
                            if (matcher1.find()) {
                                purl = matcher.group(0) + matcher1.group(0);
                            }
                        }
                    }
                    purl = purl.replace("..", ".").trim();
                    if (!purl.isEmpty() && !parseUrls.contains(purl))
                        parseUrls.add(purl);

                }
            } catch (Exception e) {
                SpiderDebug.log(e);
            }
        }
        vod.put("vod_play_from", TextUtils.join("$$$", playFlags));
        vod.put("vod_play_url", TextUtils.join("$$$", playUrls));
    }

    // ######视频地址
    protected JSONObject getFinalVideo(String flag, ArrayList<String> parseUrls, String url) throws JSONException {
        for (String parseUrl : parseUrls) {
            if (parseUrl.isEmpty() || parseUrl.equals("null"))
                continue;
            String videoUrl = url;
            if (!isVideoFormat(url)){
                String playUrl = parseUrl + url;
                String content = OkHttpUtil.string(playUrl, null);
                JSONObject jsonObject = new JSONObject(content);
                videoUrl = jsonObject.optString("url");
                if (jsonObject.optInt("encryption") == 1) {
                    videoUrl = decryptRC4(videoUrl, getDecryptKey());
                }
            }
            JSONObject result = new JSONObject();
            result.put("parse", 0);
            result.put("playUrl", "");
            result.put("url", videoUrl);
            return result;
        }
        return null;
    }

    @Override
    public boolean manualVideoCheck() {
        return true;
    }

    @Override
    public boolean isVideoFormat(String url) {
        return Misc.isVideoFormat(url);
    }


    private String[] extInfos = null;


    /**
     * md5 32位
     */
    public String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes(StandardCharsets.UTF_8));
            byte[] encryption = md5.digest();

            StringBuilder strBuf = new StringBuilder();
            for (byte b : encryption) {
                if (Integer.toHexString(0xff & b).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & b));
                } else {
                    strBuf.append(Integer.toHexString(0xff & b));
                }
            }

            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private String getDecryptKey() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
        Date date = new Date();
        String today = simpleDateFormat.format(date);
        String md5 = getMD5(today);
        md5 = sort(md5);
        md5 = getMD5(md5);
        return md5.substring(1, 9);
    }

    public static String sort(String paramString) {
        char[] arrayOfChar = paramString.toCharArray();
        Arrays.sort(arrayOfChar);
        StringBuilder stringBuilder = new StringBuilder();
        int i = arrayOfChar.length;
        for (byte b = 0; b < i; b++)
            stringBuilder.append(arrayOfChar[b]);
        return stringBuilder.toString();
    }

    private String buLeiApiKey() {
        Date paramDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(paramDate);
        String str3 = key;
        String year = String.valueOf(calendar.get(1));
        String hour = String.valueOf(calendar.get(11));
        String minute = String.valueOf(calendar.get(12));
        String str1 = hour;
        if (hour.length() < 2) {
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append("0");
            stringBuilder1.append(hour);
            str1 = stringBuilder1.toString();
        }
        hour = minute;
        if (minute.length() < 2) {
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append("0");
            stringBuilder1.append(minute);
            hour = stringBuilder1.toString();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(year);
        stringBuilder.append(":");
        stringBuilder.append(str1);
        stringBuilder.append(":");
        stringBuilder.append(year);
        stringBuilder.append(":");
        stringBuilder.append(hour);
        stringBuilder.append(":");
        stringBuilder.append(str3);
        return getMD5(stringBuilder.toString());
    }

    private String decryptRC4(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return new String(RC4Base(HexString2Bytes(data), key));
    }

    private byte[] RC4Base(byte[] input, String mKkey) {
        int x = 0;
        int y = 0;
        byte key[] = initKey(mKkey);
        int xorIndex;
        byte[] result = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            x = (x + 1) & 0xff;
            y = ((key[x] & 0xff) + y) & 0xff;
            byte tmp = key[x];
            key[x] = key[y];
            key[y] = tmp;
            xorIndex = ((key[x] & 0xff) + (key[y] & 0xff)) & 0xff;
            result[i] = (byte) (input[i] ^ key[xorIndex]);
        }
        return result;
    }

    private static byte[] initKey(String aKey) {
        byte[] b_key = aKey.getBytes();
        byte state[] = new byte[256];

        for (int i = 0; i < 256; i++) {
            state[i] = (byte) i;
        }
        int index1 = 0;
        int index2 = 0;
        if (b_key == null || b_key.length == 0) {
            return null;
        }
        for (int i = 0; i < 256; i++) {
            index2 = ((b_key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;
            byte tmp = state[i];
            state[i] = state[index2];
            state[index2] = tmp;
            index1 = (index1 + 1) % b_key.length;
        }
        return state;
    }

    private byte[] HexString2Bytes(String src) {
        int size = src.length();
        byte[] ret = new byte[size / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < size / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    private byte uniteBytes(byte src0, byte src1) {
        char _b0 = (char) Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (char) (_b0 << 4);
        char _b1 = (char) Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

}