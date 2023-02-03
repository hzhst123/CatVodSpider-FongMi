package com.github.catvod.spider;

import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.AES;
import com.github.catvod.utils.CBC;
import com.github.catvod.utils.gZip;
import com.github.catvod.utils.okhttp.OKCallBack;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Czsapp extends Spider {
    private static final Pattern Y = Pattern.compile("\"([^\"]+)\";var [\\d\\w]+=function dncry.*md5.enc.Utf8.parse\\(\"([\\d\\w]+)\".*md5.enc.Utf8.parse\\(([\\d]+)\\)");
    private static final Pattern pY = Pattern.compile("video: \\{url: \"([^\"]+)\"");
    private static final Pattern m = Pattern.compile("subtitle: \\{url:\"([^\"]+\\.vtt)\"");
    private static final Pattern Q = Pattern.compile("src: '([^']+\\.css)',");
    private static final Pattern Db = Pattern.compile("/movie/(\\d+).html");
    private static final Pattern I = Pattern.compile("/page/(\\d+)");
    private static final Pattern d = Pattern.compile("/v_play/(.*)\\.html");
    private static final Pattern K = Pattern.compile("var vkey = ['\"]([^'\"]+)['\"]");
    private static final Pattern Oe = Pattern.compile("var fvkey = ['\"]([^'\"]+)['\"]");
    private static final Pattern fi = Pattern.compile("var ua = ['\"]([^'\"]+)['\"]");
    private static final Pattern M = Pattern.compile("var cip = ['\"]([^'\"]+)['\"]");
    private static final Pattern X = Pattern.compile("var time = ['\"]([^'\"]+)['\"]");
    private static final Pattern a = Pattern.compile("var url = ['\"]([^'\"]+)['\"]");


    private String mmd5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes());
            byte[] digest = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hexString = Integer.toHexString(b & 255);
                while (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Object[] loadsub(String sub) {
        try {
            OKCallBack.OKCallBackDefault callBack = new OKCallBack.OKCallBackDefault() {
                @Override
                protected void onFailure(Call call, Exception e) {

                }

                @Override
                protected void onResponse(Response response) {

                }
            };
            OkHttpClient YM = OkHttpUtil.defaultClient();
            OkHttpUtil.get(YM, sub, null, getHeaders(), callBack);
            Response result = callBack.getResult();
            int code = result.code();
            if (code == 404) {
                return new Object[]{200, "application/octet-stream", new ByteArrayInputStream("WEBVTT".getBytes())};
            }
            ResponseBody body = result.body();
            byte[] bytes = body.bytes();
            byte[] tokenkey = Arrays.copyOfRange(bytes, 0, 16);
            byte[] data = Arrays.copyOfRange(bytes, 16, bytes.length);
            byte[] KS = CBC.CBC(data, tokenkey, tokenkey);
            String vtt = gZip.KS(KS);


            vtt = vtt.replaceAll("(\\d{2}:\\d{2}:\\d{2}.\\d{3}.+\\d{2}:\\d{2}:\\d{2}.\\d{3}).*", "$1");
            vtt = vtt.replaceAll("(\\d{2}:\\d{2}.\\d{3}).*?( --> )(\\d{2}:\\d{2}.\\d{3}).*", "00:$1$200:$3");
            vtt = vtt.replaceAll("<.*><.*>(.*)<.*><.*>", "$1");
            vtt = vtt.replaceAll("&(.*);", "");
            vtt = vtt.replaceAll(".*NOTE.*", "");
            BufferedReader br = new BufferedReader(new StringReader(vtt));
            ArrayList<String> lines = new ArrayList<>();
            int captionNumber = 1;
            String line = br.readLine();
            while (line != null) {
                if (line.matches("\\d{2}:\\d{2}:\\d{2}.\\d{3}.+\\d{2}:\\d{2}:\\d{2}.\\d{3}")) {
                    if (lines.get(lines.size() - 1).trim().isEmpty()) {
                        lines.add(captionNumber + "");
                        captionNumber++;
                    }
                }
                lines.add(line);
                line = br.readLine();
            }
            String join = TextUtils.join("\n", lines);

            return new Object[]{200, "application/octet-stream", new ByteArrayInputStream(join.getBytes())};
        } catch (Exception e) {
            e.printStackTrace();
            SpiderDebug.log(e);
            return null;
        }
    }

    public String categoryContent(String str, String str2, boolean z, HashMap<String, String> hashMap) {
        try {
            JSONObject jSONObject = new JSONObject();
            Pattern btwaf = Pattern.compile("(?<=btwaf=).*?(?=\")");
            String Contentw = OkHttpUtil.string("https://www.c-zzy.com/" + str + "/page/" + str2, getHeaders());
            Document doc = null;
            if (Contentw.contains("btwaf")) {
                Matcher bttime1 = btwaf.matcher(Contentw);
                String bttime = "";
                if (bttime1.find()) {
                    bttime = bttime1.group(0);
                }
                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/" + str + "/page/" + str2 + "?btwaf=" + bttime, getHeaders()));

            }

            if (Contentw.contains("huadong") || Contentw.contains("renji")) {
                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/" + str + "/page/" + str2, Headers()));
            } else {
                doc = Jsoup.parse(Contentw);
            }
            int parseInt = Integer.parseInt(str2);
            int parseInt2 = Integer.parseInt(str2);
            Matcher matcher = I.matcher(doc.select("div.pagenavi_txt > a.extend").last().attr("href"));
            if (matcher.find()) {
                parseInt2 = Integer.parseInt(matcher.group(1));
            }
            Elements jS = doc.select("div.mi_ne_kd > ul > li");
            JSONArray jSONArray = new JSONArray();
            for (Element next : jS) {
                Matcher matcher2 = Db.matcher(next.select("a").attr("href"));
                if (matcher2.find()) {
                    String group = matcher2.group(1);
                    String trim = next.select("img").attr("alt").trim();
                    String trim2 = next.select("img").attr("data-original").trim();
                    String trim3 = next.select("div.hdinfo > span").text().trim();
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("vod_id", group);
                    jSONObject2.put("vod_name", trim);
                    jSONObject2.put("vod_pic", trim2);
                    jSONObject2.put("vod_remarks", trim3);
                    jSONArray.put(jSONObject2);
                }
            }
            jSONObject.put("list", jSONArray);
            jSONObject.put("page", parseInt);
            jSONObject.put("pagecount", parseInt2);
            jSONObject.put("limit", 24);
            jSONObject.put("total", parseInt2 <= 1 ? jSONArray.length() : parseInt2 * 24);
            return jSONObject.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    public String detailContent(List<String> list) {
        String str2 = "";
        try {

            JSONObject jSONObject = new JSONObject();
            JSONArray jSONArray = new JSONArray();
            JSONObject jSONObject2 = new JSONObject();
            Pattern btwaf = Pattern.compile("(?<=btwaf=).*?(?=\")");
            String Contentw = OkHttpUtil.string("https://www.c-zzy.com/movie/" + list.get(0) + ".html", getHeaders());
            Document doc = null;
            if (Contentw.contains("btwaf")) {
                Matcher bttime1 = btwaf.matcher(Contentw);
                String bttime = "";
                if (bttime1.find()) {
                    bttime = bttime1.group(0);
                }
                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/movie/" + list.get(0) + ".html?btwaf=" + bttime, getHeaders()));

            }

            if (Contentw.contains("huadong") || Contentw.contains("renji")) {
                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/movie/" + list.get(0) + ".html", Headers()));
            } else {
                doc = Jsoup.parse(Contentw);
            }
            String trim = doc.select("div.moviedteail_tt > h1").text().trim();
            String pY2 = doc.select("div.dyimg > img").attr("src");
            Iterator<Element> it = doc.select("ul.moviedteail_list > li").iterator();
            String str3 = str2;
            String str4 = str3;
            String str5 = str4;
            String str6 = str5;
            String str7 = str6;
            String str8 = str7;
            while (it.hasNext()) {
                String trim2 = it.next().text().trim();
                if (trim2.length() >= 4) {
                    try {
                        String substring = trim2.substring(0, 2);
                        String substring2 = trim2.substring(3);
                        if (substring.equals("类型")) {
                            str3 = substring2;
                        } else if (substring.equals("地区")) {
                            str5 = substring2;
                        } else if (substring.equals("年份")) {
                            str4 = substring2;
                        } else if (substring.equals("导演")) {
                            str8 = substring2;
                        } else if (substring.equals("主演")) {
                            str7 = substring2;
                        } else if (substring.equals("豆瓣")) {
                            str6 = substring2;
                        }
                    } catch (Exception e) {
                        SpiderDebug.log(e);
                    }
                }
            }
            jSONObject2.put("vod_id", list.get(0));
            jSONObject2.put("vod_name", trim);
            jSONObject2.put("vod_pic", pY2);
            jSONObject2.put("type_name", str3);
            jSONObject2.put("vod_year", str4);
            jSONObject2.put("vod_area", str5);
            jSONObject2.put("vod_remarks", str6);
            jSONObject2.put("vod_actor", str7);
            jSONObject2.put("vod_director", str8);
            jSONObject2.put("vod_content", doc.select("div.yp_context").text().trim());
            jSONObject2.put("vod_play_from", "厂长资源");
            ArrayList arrayList = new ArrayList();
            for (Element next : doc.select("div.paly_list_btn > a")) {
                Matcher matcher = d.matcher(next.attr("href"));
                if (matcher.find()) {
                    arrayList.add(next.text() + "$" + matcher.group(1));
                }
            }
            jSONObject2.put("vod_play_url", TextUtils.join("#", arrayList));
            jSONArray.put(jSONObject2);
            jSONObject.put("list", jSONArray);
            return jSONObject.toString();
        } catch (Exception e3) {
        }
        return "";
    }

    public String homeContent(boolean z) {
        try {
            JSONObject jSONObject = new JSONObject();
            Pattern btwaf = Pattern.compile("(?<=btwaf=).*?(?=\")");
            String Contentw = OkHttpUtil.string("https://www.c-zzy.com/", getHeaders());
            Document doc = null;
            if (Contentw.contains("btwaf")) {
                Matcher bttime1 = btwaf.matcher(Contentw);
                String bttime = "";
                if (bttime1.find()) {
                    bttime = bttime1.group(0);
                }
                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/?btwaf=" + bttime, getHeaders()));

            }

            if (Contentw.contains("huadong") || Contentw.contains("renji")) {
                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/", Headers()));
            } else {
                doc = Jsoup.parse(Contentw);
            }

            Elements jS = doc.select(".navlist > li > a");
            JSONArray jSONArray = new JSONArray();
            for (Element next : jS) {
                String pY2 = next.attr("href");
                if (pY2.length() > 1) {
                    String substring = pY2.substring(1);
                    String trim = next.text().trim();
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("type_id", substring);
                    jSONObject2.put("type_name", trim);
                    jSONArray.put(jSONObject2);
                }
            }
            jSONObject.put("class", jSONArray);
            Elements jS2 = doc.select("div.mi_ne_kd > ul > li");
            JSONArray jSONArray2 = new JSONArray();
            for (Element next2 : jS2) {
                Matcher matcher = Db.matcher(next2.select("a").attr("href"));
                if (matcher.find()) {
                    String group = matcher.group(1);
                    String trim2 = next2.select("img").attr("alt").trim();
                    String trim3 = next2.select("img").attr("data-original").trim();
                    String trim4 = next2.select("div.hdinfo > span").text().trim();
                    JSONObject jSONObject3 = new JSONObject();
                    jSONObject3.put("vod_id", group);
                    jSONObject3.put("vod_name", trim2);
                    jSONObject3.put("vod_pic", trim3);
                    jSONObject3.put("vod_remarks", trim4);
                    jSONArray2.put(jSONObject3);
                }
            }
            jSONObject.put("list", jSONArray2);
            return jSONObject.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    protected static HashMap<String, String> getHeaders() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("User-Agent", "Mozilla/5.0 (iPad; CPU OS 13_2_3 like Mac OS X) AppleWebKit/537.36  (KHTML, like Gecko) Version/13.0 Mobile/13B14 Safari/537.36");
        hashMap.put("referer", "https://www.c-zzy.com");
        return hashMap;
    }

    protected static HashMap<String, String> getHeaders2() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("User-Agent", "Mozilla/5.0 (iPad; CPU OS 13_2_3 like Mac OS X) AppleWebKit/537.36  (KHTML, like Gecko) Version/13.0 Mobile/13B14 Safari/537.36");
        hashMap.put("Cookie", "myannoun=1; Hm_lvt_c08e84f2c697dc9d0af77ff0dbfb3d6d=1669043393; Hm_lvt_d06dda04a24e89e1117ee1455e217c30=1669043393; f205ae596d47822115b3bf705ba37939=9f598570cf23f2141c4a88b7f6d09dc8; f399be52374d54328203a4f0905e6c39=cf6fac551369df290f4b54b6fb7c5cba; e29339bfab011d9157d77252db007f7d=f1ee64c7e039dbc6911fd26271af1775; 0f94935cfd2809e8704598034706f10b=84fa4378b2233dc38c3ab0f780bfb3eb; esc_search_captcha=1; result=30; Hm_lpvt_c08e84f2c697dc9d0af77ff0dbfb3d6d=1670218593; Hm_lpvt_d06dda04a24e89e1117ee1455e217c30=1670218593");
        return hashMap;
    }


    protected static HashMap<String, String> Headers() throws IOException {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("User-Agent", "Mozilla/5.0 (iPad; CPU OS 13_2_3 like Mac OS X) AppleWebKit/537.36  (KHTML, like Gecko) Version/13.0 Mobile/13B14 Safari/537.36");
        hashMap.put("referer", "https://www.c-zzy.com");
        Pattern id = Pattern.compile("(?<=src=\"/).*?(?=\")");
        Pattern key = Pattern.compile("(?<=var key=\").*?(?=\")");
        Pattern key2 = Pattern.compile("(?<=key=\").*?(?=\")");


        String ids = "";

        String jskeys = "";
        String cookie = "";

        String Content = OkHttpUtil.string("https://www.c-zzy.com/", getHeaders());

        Matcher matcher1 = id.matcher(Content);
        if (matcher1.find()) {
            ids = matcher1.group(0);
        }

        if (ids.contains("huadong")) {
            String jsurl = "https://www.c-zzy.com/" + ids;
            String jskey = OkHttpUtil.string(jsurl, getHeaders());
            Matcher matcher2 = key2.matcher(jskey);
            if (matcher2.find()) {
                jskeys = matcher2.group(0);
            }
            URL url = new URL("https://www.c-zzy.com/a20be899_96a6_40b2_88ba_32f1f75f1552_yanzheng_huadong.php?type=ad82060c2e67cc7e2cc47552a4fc1242&key=" + jskeys + "&value=8174fb7ee891963498d5dbeddf20bc53");
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (iPad; CPU OS 13_2_3 like Mac OS X) AppleWebKit/537.36  (KHTML, like Gecko) Version/13.0 Mobile/13B14 Safari/537.36");
            conn.setRequestProperty("Referer", "https://www.c-zzy.com/");


            cookie = conn.getHeaderField("Set-Cookie");
        }
        if (ids.contains("renji")) {
            String jsurl2 = "https://www.c-zzy.com/" + ids;
            String jskey = OkHttpUtil.string(jsurl2, getHeaders());
            Matcher matcher3 = key.matcher(jskey);
            if (matcher3.find()) {
                jskeys = matcher3.group(0);
            }
            URL url = new URL("https://www.c-zzy.com/a20be899_96a6_40b2_88ba_32f1f75f1552_yanzheng_ip.php?type=96c4e20a0e951f471d32dae103e83881&key=" + jskeys + "&value=b9c1b06534ddb490f6d5bfcf6d038bc8");

            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (iPad; CPU OS 13_2_3 like Mac OS X) AppleWebKit/537.36  (KHTML, like Gecko) Version/13.0 Mobile/13B14 Safari/537.36");
            conn.setRequestProperty("Referer", "https://www.c-zzy.com/");
            cookie = conn.getHeaderField("Set-Cookie");

        } else {
            cookie = null;
        }

        if (cookie != null || cookie.length() >= 1) {

            hashMap.put("cookie", cookie);
        }
        return hashMap;
    }


    public String playerContent(String str, String str2, List<String> list) {
        String str3;
        String str4;
        Elements G8;
        try {
            String K2 = "";
            Pattern btwaf = Pattern.compile("(?<=btwaf=).*?(?=\")");
            String KK3 = OkHttpUtil.string("https://www.c-zzy.com/v_play/" + str2 + ".html", getHeaders());


            if (KK3.contains("btwaf")) {
                Matcher bttime1 = btwaf.matcher(KK3);
                String bttime = "";
                if (bttime1.find()) {
                    bttime = bttime1.group(0);
                }
                K2 = OkHttpUtil.string("https://www.c-zzy.com/v_play/" + str2 + ".html?btwaf=" + bttime, getHeaders());

            }

            if (KK3.contains("huadong") || KK3.contains("renji")) {
                K2 = OkHttpUtil.string("https://www.c-zzy.com/v_play/" + str2 + ".html", Headers());
            } else {
                K2 = KK3;
            }
            Document UR = Jsoup.parse(K2);
            Matcher matcher = Y.matcher(K2);
            if (matcher.find()) {
                String group = matcher.group(1);
                String KEY = matcher.group(2);
                String IV = matcher.group(3);
                String str5 = AES.CBC(group, KEY, IV);
                Matcher matcher2 = pY.matcher(str5);
                str3 = matcher2.find() ? matcher2.group(1) : "";
                Matcher matcher3 = m.matcher(str5);
                str4 = matcher3.find() ? matcher3.group(1) : "";
            } else {
                str4 = "";
                str3 = str4;
            }
            if (TextUtils.isEmpty(str3) && (G8 = UR.select("iframe.viframe")) != null) {
                String pY2 = G8.attr("src");
                if (pY2.contains("jx.xmflv.com")) {
                    String K3 = OkHttpUtil.string(pY2, getHeaders());
                    Matcher matcher4 = X.matcher(K3);
                    if (!matcher4.find()) {
                        return "";
                    }
                    String group2 = matcher4.group(1);
                    Matcher matcher5 = a.matcher(K3);
                    if (!matcher5.find()) {
                        return "";
                    }
                    String group3 = matcher5.group(1);
                    String K4 = OkHttpUtil.string("https://jx.xmflv.com/player.php?time=" + group2 + "&url=" + group3, getHeaders());
                    Matcher matcher6 = K.matcher(K4);
                    if (!matcher6.find()) {
                        return "";
                    }
                    String group4 = matcher6.group(1);
                    Matcher matcher7 = Oe.matcher(K4);
                    if (!matcher7.find()) {
                        return "";
                    }
                    String group5 = matcher7.group(1);
                    Matcher matcher8 = fi.matcher(K4);
                    if (!matcher8.find()) {
                        return "";
                    }
                    String group6 = matcher8.group(1);
                    Matcher matcher9 = M.matcher(K4);
                    if (!matcher9.find()) {
                        return "";
                    }
                    String group7 = matcher9.group(1);
                    Matcher matcher10 = X.matcher(K4);
                    if (!matcher10.find()) {
                        return "";
                    }
                    String group8 = matcher10.group(1);
                    byte[] bytes3 = mmd5(group5).getBytes();
                    byte[] bytes4 = "UVE1NTY4MDY2NQ==".getBytes();
                    Cipher cipher2 = Cipher.getInstance("AES/CBC/NoPadding");
                    cipher2.init(1, new SecretKeySpec(bytes3, "AES"), new IvParameterSpec(bytes4));
                    String encodeToString = Base64.encodeToString(cipher2.doFinal(group5.getBytes()), 0);
                    OKCallBack.OKCallBackDefault callBack = new OKCallBack.OKCallBackDefault() {

                        public void onResponse(Response response) {
                        }

                        @Override
                        protected void onFailure(Call call, Exception exc) {
                        }
                    };
                    HashMap hashMap = new HashMap();
                    hashMap.put("url", group3);
                    hashMap.put("time", group8);
                    hashMap.put("ua", group6);
                    hashMap.put("cip", group7);
                    hashMap.put("vkey", group4);
                    hashMap.put("fvkey", encodeToString);
                    OkHttpUtil.post(OkHttpUtil.defaultClient(), "https://jx.xmflv.com/xmflv-1.SVG", hashMap, getHeaders(), callBack);
                    str3 = new JSONObject(callBack.getResult().body().string()).getString("url");
                } else {
                    Matcher matcher11 = Q.matcher(OkHttpUtil.string(pY2, getHeaders()));
                    if (matcher11.find()) {
                        str3 = matcher11.group(1);
                    }
                }
            }
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("parse", "0");
            jSONObject.put("playUrl", "");
            jSONObject.put("url", str3);
            jSONObject.put("header", "");
            if (!TextUtils.isEmpty(str4)) {
                jSONObject.put("subf", "/vtt/utf-8");
                jSONObject.put("subt", Proxy.getUrl() + "?do=czspp&url=" + URLEncoder.encode(str4));
            }
            return jSONObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            SpiderDebug.log(e);
            return "";
        }
    }

    public String searchContent(String str, boolean z) {
        try {
            JSONObject jSONObject = new JSONObject();
            Pattern btwaf = Pattern.compile("(?<=btwaf=).*?(?=\")");
            Document doc = null;
            String Contents = OkHttpUtil.string("https://www.c-zzy.com/?s=大厂长大大求饶命&btwaf=93094563", getHeaders());
            String bttimeo = "";

            if (Contents.contains("btwaf")) {
                Matcher bttime = btwaf.matcher(Contents);

                if (bttime.find()) {
                    bttimeo = bttime.group(0);
                }

                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/?s=" + URLEncoder.encode(str) + "&btwaf=" + bttimeo, getHeaders2()));
            }
            if (Contents.contains("huadong") || Contents.contains("renji")) {
                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/?s=" + URLEncoder.encode(str), Headers()));
            } else {
                doc = Jsoup.parse(OkHttpUtil.string("https://www.c-zzy.com/?s=" + URLEncoder.encode(str), getHeaders()));
            }
           ;
            Elements jS = doc.select("div.mi_ne_kd > ul > li");
            JSONArray jSONArray = new JSONArray();
            for (Element next : jS) {
                Matcher matcher = Db.matcher(next.select("a").attr("href"));
                JSONObject jSONObject2 = new JSONObject();
                if (matcher.find()) {
                    String group = matcher.group(1);
                    String trim = next.select("img").attr("alt").trim();
                    if (trim.contains(str)) {
                        String trim2 = next.select("img").attr("src").trim();
                        if (next.hasClass(".jidi")) {
                            String remark = next.select(".jidi span").text();
                            jSONObject2.put("vod_remarks", remark);
                        } else {
                            String id = next.select("a").attr("href");
                            Document doc1 = Jsoup.parse(OkHttpUtil.string(id, getHeaders()));
                            String remark = doc1.select("ul.moviedteail_list li span").get(0).text();
                            jSONObject2.put("vod_id", group);
                            jSONObject2.put("vod_name", trim);
                            jSONObject2.put("vod_pic", trim2);
                            jSONObject2.put("vod_remarks", remark);
                            jSONArray.put(jSONObject2);
                        }
                    }
                }
            }
            jSONObject.put("list", jSONArray);
            return jSONObject.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }
}
