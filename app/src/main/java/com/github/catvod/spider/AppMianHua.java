package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.AES;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Author: @SDL
 */
public class AppMianHua extends Spider {

    private String siteUrl = "";
    private String key = "";
    private String iv = "";

    private HashMap<String, String> getHeaders() {
        HashMap hashMap = new HashMap();
        String time = String.valueOf((new Date().getTime() / 1000L));
        String json = "{\"version\":\"13\",\"client_name\":\"mianhua\",\"time_stamp\":\"" + time + "\"}";
        String signature = encrypt(json, key, iv);
        hashMap.put("signature", signature);
        hashMap.put("clientname", "mianhua");
        hashMap.put("version", "13");
        hashMap.put("User-Agent", "okhttp/3.14.9");
        return hashMap;
    }

    private String encrypt(String src, String KEY, String IV) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec);
            return Base64.encodeToString(cipher.doFinal(src.getBytes()), Base64.NO_WRAP);
        } catch (Exception exception) {
            SpiderDebug.log(exception);
        }
        return null;
    }

    private String postString(String url, Map<String, String> paramsMap, Map<String, String> headerMap) {

        Request.Builder builder = new Request.Builder().url(url)
                .post(getRequestBody(paramsMap));
        if (headerMap != null) {
            for (String key : headerMap.keySet()) {
                builder.addHeader(key, headerMap.get(key));
            }

        }
        Request request = builder.build();
        Call call = OkHttpUtil.defaultClient().newCall(request);
        try {
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private RequestBody getRequestBody(Map<String, String> paramsMap) {

        FormBody.Builder formBody = new FormBody.Builder();
        if (paramsMap != null) {
            for (String key : paramsMap.keySet()) {
                formBody.add(key, paramsMap.get(key));
            }
        }
        return formBody.build();
    }


    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        String str[] = extend.split("\\$\\$\\$");
        siteUrl = str[0];
        key = new String(Base64.decode(str[1], Base64.NO_WRAP));
        iv = new String(Base64.decode(str[2], Base64.NO_WRAP));
    }

    @Override
    public String homeContent(boolean filter) {
        try {

            JSONArray classes = new JSONArray();
            JSONObject jSONObject1 = new JSONObject();
            jSONObject1.put("type_id", "waiju");
            jSONObject1.put("type_name", "外剧");
            classes.put(jSONObject1);

            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("type_id", "dianying");
            jSONObject2.put("type_name", "电影");
            classes.put(jSONObject2);

            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("type_id", "dianshiju");
            jSONObject3.put("type_name", "电视剧");
            classes.put(jSONObject3);

            JSONObject jSONObject4 = new JSONObject();
            jSONObject4.put("type_id", "dongman");
            jSONObject4.put("type_name", "动漫");
            classes.put(jSONObject4);

            JSONObject jSONObject5 = new JSONObject();
            jSONObject5.put("type_id", "zongyi");
            jSONObject5.put("type_name", "综艺");
            classes.put(jSONObject5);

            JSONObject result = new JSONObject();
            result.put("class", classes);

            return result.toString();
        } catch (Throwable th) {

        }
        return "";
    }

    @Override
    public String homeVideoContent() {
        try {
            JSONArray videos = new JSONArray();
            try {
                String url = siteUrl + "index/index";
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("page", "1");
                paramsMap.put("type", "tuijian");
                String data = postString(url, paramsMap, getHeaders());
                JSONObject jsonObject = new JSONObject(decryptResponse(data));
                JSONArray jsonArray = jsonObject.getJSONArray("banners");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject blockObj = jsonArray.getJSONObject(i);
                    JSONObject v = new JSONObject();
                    v.put("vod_id", blockObj.getString("id"));
                    v.put("vod_name", blockObj.getString("title"));
                    v.put("vod_pic", blockObj.getString("banner_url"));
                    v.put("vod_remarks", "");
                    videos.put(v);
                }
                JSONArray jsonArrayVideos = jsonObject.getJSONArray("list");
                for (int i = 0; i < jsonArrayVideos.length(); i++) {
                    JSONObject jList = jsonArrayVideos.getJSONObject(i);
                    JSONArray jVideos = jList.getJSONArray("vidoes");
                    for (int k = 0; k < jVideos.length(); k++) {
                        JSONObject blockObj = jVideos.getJSONObject(k);
                        JSONObject v = new JSONObject();
                        v.put("vod_id", blockObj.getString("id"));
                        v.put("vod_name", blockObj.getString("title"));
                        v.put("vod_pic", blockObj.getString("pic"));
                        v.put("vod_remarks", "");
                        videos.put(v);
                    }
                }
            } catch (Exception e) {
            }
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            JSONArray videos = new JSONArray();
            try {
                String url = siteUrl + "index/index";
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("page", "1");
                paramsMap.put("type", tid);
                String data = postString(url, paramsMap, getHeaders());
                JSONObject jsonObject = new JSONObject(decryptResponse(data));

                JSONArray jsonArray = jsonObject.getJSONArray("banners");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject blockObj = jsonArray.getJSONObject(i);
                    JSONObject v = new JSONObject();
                    v.put("vod_id", blockObj.getString("id"));
                    v.put("vod_name", blockObj.getString("title"));
                    v.put("vod_pic", blockObj.getString("banner_url"));
                    v.put("vod_remarks", "");
                    videos.put(v);
                }
                JSONArray jsonArrayVideos = jsonObject.getJSONArray("list");
                for (int i = 0; i < jsonArrayVideos.length(); i++) {
                    JSONObject jList = jsonArrayVideos.getJSONObject(i);
                    JSONArray jVideos = jList.getJSONArray("vidoes");
                    for (int k = 0; k < jVideos.length(); k++) {
                        JSONObject blockObj = jVideos.getJSONObject(k);
                        JSONObject v = new JSONObject();
                        v.put("vod_id", blockObj.getString("id"));
                        v.put("vod_name", blockObj.getString("title"));
                        v.put("vod_pic", blockObj.getString("pic"));
                        v.put("vod_remarks", "");
                        videos.put(v);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return "";
    }

    @Override
    public String detailContent(List<String> ids) {
        try {

            String url = siteUrl + "index/detail";
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("id", ids.get(0));
            String data = postString(url, paramsMap, getHeaders());

            JSONObject result = new JSONObject();
            JSONArray list = new JSONArray();

            JSONObject videoInfo = new JSONObject(decryptResponse(data)).getJSONObject("detail");

            JSONObject vodAtom = new JSONObject();
            vodAtom.put("vod_id", videoInfo.getString("id"));
            vodAtom.put("vod_name", videoInfo.getString("title"));
            vodAtom.put("vod_pic", videoInfo.getString("pic"));
            vodAtom.put("vod_year", videoInfo.getString("year"));
            vodAtom.put("vod_remarks", videoInfo.getString("remarks"));
            JSONArray areaJson = videoInfo.getJSONArray("area");
            List<String> areaList = new ArrayList();
            for (int i = 0; i < areaJson.length(); i++) {
                areaList.add(areaJson.getString(i));
            }
            vodAtom.put("vod_area", TextUtils.join(",", areaList));

            JSONArray actorJson = videoInfo.getJSONArray("actors");
            List<String> actorList = new ArrayList();
            for (int i = 0; i < actorJson.length(); i++) {
                actorList.add(actorJson.getString(i));
            }
            vodAtom.put("vod_area", TextUtils.join(",", actorList));


            JSONArray directorJson = videoInfo.getJSONArray("director");
            List<String> directorList = new ArrayList();
            for (int i = 0; i < directorJson.length(); i++) {
                directorList.add(directorJson.getString(i));
            }
            vodAtom.put("vod_director", TextUtils.join(",", directorList));
            vodAtom.put("vod_content", videoInfo.getString("blurb").trim());

            JSONArray episodes = videoInfo.getJSONArray("players");
            List<String> parsesList = new ArrayList<>();
            List<String> showList = new ArrayList<>();
            for (int i = 0; i < episodes.length(); i++) {
                JSONObject play = episodes.getJSONObject(i);
                String show = play.getString("name");
                String key = play.getString("key");
                showList.add(show);
                JSONArray datas = play.getJSONArray("datas");
                List<String> playUrls = new ArrayList<>();
                for (int k = 0; k < datas.length(); k++) {
                    JSONObject playdata = datas.getJSONObject(k);
                    String videoUrl = playdata.getString("play_url");
                    String name = playdata.getString("text");
                    playUrls.add(name + "$" + key + "|" + videoUrl);
                }
                parsesList.add(TextUtils.join("#", playUrls));
            }
            vodAtom.put("vod_play_from", TextUtils.join("$$$", showList));
            vodAtom.put("vod_play_url", TextUtils.join("$$$", parsesList));
            list.put(vodAtom);
            result.put("list", list);
            return result.toString();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        String[] urls = id.split("\\|");
        String key = urls[0];
        try {
            if (key.equals("js") || id.contains(".m3u8") || id.contains(".mp4")) {
                JSONObject result = new JSONObject();
                result.put("parse", 0);
                result.put("playUrl", "");
                result.put("url", urls[1]);
                return result.toString();
            }
            getParseList();
            ParseModel parseModel = null;
            for (int i = 0; i < parseList.size(); i++) {
                parseModel = parseList.get(i);
                boolean isOk = false;
                for (int k = 0; k < parseModel.keyList.length; k++) {
                    if (parseModel.keyList[k].equals(key)) {
                        isOk = true;
                        break;
                    }
                }
                if (isOk) {
                    break;
                }
            }
            if (parseModel == null) {
                return "";
            }
            JSONObject dataObject = new JSONObject(OkHttpUtil.string(parseModel.url + urls[1], getHeaders()));
            String playUrl = dataObject.optString("url");
            JSONObject result = new JSONObject();
            result.put("parse", 0);
            result.put("playUrl", "");
            result.put("url", playUrl);
            return result.toString();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String url = siteUrl + "search/searchvideo";
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("page", "1");
            paramsMap.put("pagesize", "10");
            paramsMap.put("keyword", key);
            String data = postString(url, paramsMap, getHeaders());
            JSONArray jsonArray = new JSONArray(decryptResponse(data));
            JSONArray videos = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject blockObj = jsonArray.getJSONObject(i);
                JSONObject v = new JSONObject();
                v.put("vod_id", blockObj.getString("id"));
                v.put("vod_name", blockObj.getString("title"));
                v.put("vod_pic", blockObj.getString("pic"));
                v.put("vod_remarks", blockObj.getString("blurb"));
                videos.put(v);
            }
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return "";
    }

    protected String decryptResponse(String src) {
        try {
            JSONObject dataObject = new JSONObject(src);
            String infoJson = dataObject.getString("data");
            return AES.CBC(infoJson, key, iv);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private List<ParseModel> getParseList() {
        if (!parseList.isEmpty()) {
            return parseList;
        }
        try {
            String url = siteUrl + "common/parsing";
            Map<String, String> paramsMap = new HashMap<>();
            String data = postString(url, paramsMap, getHeaders());
            JSONArray jsonArray = new JSONArray(decryptResponse(data));
            parseList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject blockObj = jsonArray.getJSONObject(i);
                ParseModel parseModel = new ParseModel();
                parseModel.url = blockObj.getString("url");
                parseModel.keyList = blockObj.getString("play_key").split(",");
                parseList.add(parseModel);
            }

        } catch (Throwable th) {
            th.printStackTrace();
        }
        return parseList;
    }

    private List<ParseModel> parseList = new ArrayList();

    class ParseModel {
        String url;
        String[] keyList;
    }
}