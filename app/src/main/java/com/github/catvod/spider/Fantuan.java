package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.AES;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fantuan extends Spider {
    private static String SiteUrl = "";
    private JSONObject playerConfig;
    private JSONObject filterConfig;
    private final Pattern regexUrl = Pattern.compile("(?<=urls = \").+?(?=\";)");
    private Pattern regexCategory = Pattern.compile("/type/id-(\\w+).html");
    private Pattern regexVid = Pattern.compile("/detail/id-(\\w+).html");
    private Pattern regexPlay = Pattern.compile("/play/id-(\\w+)-(\\d+)-(\\d+).html");
    private Pattern regexPage = Pattern.compile("/type/id-(\\d+)-(\\d+).html");
    @Override
    public void init(Context context, String extend) {
        super.init(context);
        SiteUrl = extend;
        try {
            playerConfig = new JSONObject("{\"xg_app_player\":{\"show\":\"app全局解析\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"aliplayer\":{\"show\":\"阿里网盘\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"duoduozy\":{\"show\":\"自营极速\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"weishi\":{\"show\":\"自营蓝光\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"uploadixigua\":{\"show\":\"自营蓝光\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"gpmp4\":{\"show\":\"自营超清\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"rx\":{\"show\":\"融兴线路\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"xigua\":{\"show\":\"西瓜高清\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"mgtv\":{\"show\":\"芒果高清\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"qq\":{\"show\":\"腾讯高清\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"youku\":{\"show\":\"优酷高清\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"qiyi\":{\"show\":\"奇艺高清\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"pptv\":{\"show\":\"PPTV\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"letv\":{\"show\":\"乐视云\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"sohu\":{\"show\":\"搜狐云\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"bilibili\":{\"show\":\"哔哩云\",\"parse\":\"https://api.goodjson.top/?url=\",\"ps\":\"0\",\"or\":999},\"dbm3u8\":{\"show\":\"百度云\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"tkm3u8\":{\"show\":\"天空云\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"bdxm3u8\":{\"show\":\"北斗云\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"lym3u8\":{\"show\":\"老鸭资源\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"hnm3u8\":{\"show\":\"红牛云\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"kbm3u8\":{\"show\":\"快播云\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"wjm3u8\":{\"show\":\"无尽备用\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"dplayer\":{\"show\":\"DPlayer-H5播放器\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"videojs\":{\"show\":\"videojs-H5播放器\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"iva\":{\"show\":\"iva-H5播放器\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"iframe\":{\"show\":\"iframe外链数据\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"link\":{\"show\":\"外链数据\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"swf\":{\"show\":\"Flash文件\",\"parse\":\"\",\"ps\":\"0\",\"or\":999},\"flv\":{\"show\":\"Flv文件\",\"parse\":\"\",\"ps\":\"0\",\"or\":999}}");
            filterConfig = new JSONObject("{\"1\":[{\"key\":\"class\",\"name\":\"类型\",\"value\":[{\"n\":\"喜剧\",\"v\":\"喜剧\"},{\"n\":\"爱情\",\"v\":\"爱情\"},{\"n\":\"恐怖\",\"v\":\"恐怖\"},{\"n\":\"动作\",\"v\":\"动作\"},{\"n\":\"科幻\",\"v\":\"科幻\"},{\"n\":\"剧情\",\"v\":\"剧情\"},{\"n\":\"战争\",\"v\":\"战争\"},{\"n\":\"犯罪\",\"v\":\"犯罪\"},{\"n\":\"灾难\",\"v\":\"灾难\"},{\"n\":\"奇幻\",\"v\":\"奇幻\"},{\"n\":\"悬疑\",\"v\":\"悬疑\"},{\"n\":\"惊悚\",\"v\":\"惊悚\"},{\"n\":\"冒险\",\"v\":\"冒险\"}]},{\"key\":\"area\",\"name\":\"地区\",\"value\":[{\"n\":\"大陆\",\"v\":\"大陆\"},{\"n\":\"香港\",\"v\":\"香港\"},{\"n\":\"台湾\",\"v\":\"台湾\"},{\"n\":\"美国\",\"v\":\"美国\"},{\"n\":\"法国\",\"v\":\"法国\"},{\"n\":\"英国\",\"v\":\"英国\"},{\"n\":\"日本\",\"v\":\"日本\"},{\"n\":\"韩国\",\"v\":\"韩国\"},{\"n\":\"德国\",\"v\":\"德国\"},{\"n\":\"泰国\",\"v\":\"泰国\"},{\"n\":\"印度\",\"v\":\"印度\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"year\",\"name\":\"年份\",\"value\":[{\"n\":\"2022\",\"v\":\"2022\"},{\"n\":\"2021\",\"v\":\"2021\"},{\"n\":\"2020\",\"v\":\"2020\"},{\"n\":\"2019\",\"v\":\"2019\"},{\"n\":\"2018\",\"v\":\"2018\"},{\"n\":\"2017\",\"v\":\"2017\"},{\"n\":\"2016\",\"v\":\"2016\"},{\"n\":\"2008\",\"v\":\"2008\"},{\"n\":\"2000\",\"v\":\"2000\"},{\"n\":\"1997\",\"v\":\"1997\"},{\"n\":\"1980\",\"v\":\"1980\"}]}],\"2\":[{\"key\":\"class\",\"name\":\"类型\",\"value\":[{\"n\":\"古装\",\"v\":\"古装\"},{\"n\":\"战争\",\"v\":\"战争\"},{\"n\":\"偶像\",\"v\":\"偶像\"},{\"n\":\"犯罪\",\"v\":\"犯罪\"},{\"n\":\"奇幻\",\"v\":\"奇幻\"},{\"n\":\"剧情\",\"v\":\"剧情\"},{\"n\":\"历史\",\"v\":\"历史\"},{\"n\":\"网剧\",\"v\":\"网剧\"}]},{\"key\":\"area\",\"name\":\"地区\",\"value\":[{\"n\":\"大陆\",\"v\":\"大陆\"},{\"n\":\"香港\",\"v\":\"香港\"},{\"n\":\"台湾\",\"v\":\"台湾\"},{\"n\":\"美国\",\"v\":\"美国\"},{\"n\":\"法国\",\"v\":\"法国\"},{\"n\":\"英国\",\"v\":\"英国\"},{\"n\":\"日本\",\"v\":\"日本\"},{\"n\":\"韩国\",\"v\":\"韩国\"},{\"n\":\"德国\",\"v\":\"德国\"},{\"n\":\"泰国\",\"v\":\"泰国\"},{\"n\":\"印度\",\"v\":\"印度\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"year\",\"name\":\"年份\",\"value\":[{\"n\":\"2022\",\"v\":\"2022\"},{\"n\":\"2021\",\"v\":\"2021\"},{\"n\":\"2020\",\"v\":\"2020\"},{\"n\":\"2019\",\"v\":\"2019\"},{\"n\":\"2018\",\"v\":\"2018\"},{\"n\":\"2017\",\"v\":\"2017\"},{\"n\":\"2016\",\"v\":\"2016\"},{\"n\":\"2008\",\"v\":\"2008\"},{\"n\":\"2000\",\"v\":\"2000\"},{\"n\":\"1997\",\"v\":\"1997\"},{\"n\":\"1980\",\"v\":\"1980\"}]}]}");
        } catch (JSONException e) {
            SpiderDebug.log(e);
        }
    }

    protected HashMap<String, String>getHeaders(String url) {
        HashMap<String, String>headers = new HashMap<>();
        headers.put("method", "GET");
        if (!TextUtils.isEmpty(url)) {
            headers.put("Referer", url);
        }
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.84 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        return headers;
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            Document doc = Jsoup.parse(OkHttpUtil.string(SiteUrl, getHeaders(SiteUrl)));
            Elements elements = doc.select("ul.stui-header__menu > li > a");
            JSONArray classes = new JSONArray();
            for (Element ele: elements) {
                String name = ele.text();
                Matcher mather = regexCategory.matcher(ele.attr("href"));
                if (!mather.find())
                continue;
                String id = mather.group(1).trim();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type_id", id);
                jsonObject.put("type_name", name);
                classes.put(jsonObject);
            }
            JSONObject result = new JSONObject();
            if (filter) {
                result.put("filters", filterConfig);
            }
            result.put("class", classes);
            try {
                Elements list = doc.select("ul.stui-vodlist:nth-child(3) li div.stui-vodlist__box");
                JSONArray videos = new JSONArray();
                for (int i = 0; i < list.size(); i++) {
                    Element vod = list.get(i);
                    Matcher matcher = regexVid.matcher(vod.selectFirst("div.stui-vodlist__detail h4 a").attr("href"));
                    if (!matcher.find())
                    continue;
                    String title = vod.selectFirst(".stui-vodlist__thumb").attr("title");
                    String cover = vod.selectFirst(".stui-vodlist__thumb").attr("data-original");
                    String remark = vod.selectFirst("span.pic-text").text();
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

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String>extend) {
        try {
            String[] urlParams = new String[] {"", "", "", "", "", "", "", "", "", "", "", ""};
            String url = SiteUrl + "/type/id-";
            if (extend != null && extend.size() > 0 && extend.containsKey("tid") && extend.get("tid").length() > 0) {
                url += extend.get("tid");
            } else {
                url += tid;
            }
            urlParams[0] = tid;
            urlParams[8] = pg;
            if (extend != null && extend.size() > 0) {
                for (Iterator<String>it = extend.keySet().iterator(); it.hasNext();) {
                    String key = it.next();
                    String value = extend.get(key);
                    //urlParams[Integer.parseInt(key)] = URLEncoder.encode(value);
                    if (value.length() > 0) {
                        url += "/" + key + "/" + URLEncoder.encode(value);
                    }
                }
            }
            url += "/page/" + pg + ".html";
            String html = OkHttpUtil.string(url, getHeaders(url));
            Document doc = Jsoup.parse(html);
            JSONObject result = new JSONObject();
            int pageCount = 0;
            int page = -1;
            Elements pageInfo = doc.select("ul.stui-page__item li");
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
                    if (page == -1 && li.hasClass("active")) {
                        Matcher matcher = regexPage.matcher(a.attr("href"));
                        if (matcher.find()) {
                            page = Integer.parseInt(matcher.group(2));
                            //page = Integer.parseInt(matcher.group(2).split("-")[8]);
                        } else {
                            page = 0;
                        }
                    }
                    if (name.equals("尾页")) {
                        Matcher matcher = regexPage.matcher(a.attr("href"));
                        if (matcher.find()) {
                            pageCount = Integer.parseInt(matcher.group(2));
                            //pageCount = Integer.parseInt(matcher.group(2).split("-")[8]);
                        } else {
                            pageCount = 0;
                        }
                        break;
                    }
                }
            }
            JSONArray videos = new JSONArray();
            if (!html.contains("没有找到您想要的结果哦")) {
                Elements list = doc.select("div.stui-vodlist__box");
                for (int i = 0; i < list.size(); i++) {
                    Element vod = list.get(i);
                    String title = vod.selectFirst("a").attr("title");
                    String cover = vod.selectFirst("a").attr("data-original");
                    String remark = vod.selectFirst("a .pic-text").text();
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
            result.put("limit", 30);
            result.put("total", pageCount <= 1 ? videos.length() : pageCount * 30);
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    private static String Regex(Pattern pattern, String content) {
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

    @Override
    public String detailContent(List<String>ids) {
        try {
            String url = SiteUrl + "/detail/id-" + ids.get(0) + ".html";
            Document doc = Jsoup.parse(OkHttpUtil.string(url, getHeaders(url)));
            JSONObject result = new JSONObject();
            JSONObject vodList = new JSONObject();           
            String cover = doc.selectFirst("a.pic img").attr("data-original");
            String title = doc.selectFirst("a.pic").attr("title");
            String category = "", area = "", year = "", remark = "", director = "", actor = "", desc = "";
            Elements data = doc.select("p.data");
            desc = doc.selectFirst("span.detail-content").text().trim();
            category = Regex(Pattern.compile("类型：(\\S+)"), data.get(0).text());
            area = Regex(Pattern.compile("地区：(\\S+)"), data.get(0).text());
            year = Regex(Pattern.compile("年份：(\\S+)"), data.get(0).text());
            actor = Regex(Pattern.compile("主演：(\\S+)"), data.get(1).text());
            director = Regex(Pattern.compile("导演：(\\S+)"), data.get(1).text());
            vodList.put("vod_id", ids.get(0));
            vodList.put("vod_name", title);
            vodList.put("vod_pic", cover);
            vodList.put("type_name", category);
            vodList.put("vod_year", year);
            vodList.put("vod_area", area);
            vodList.put("vod_remarks", remark);
            vodList.put("vod_actor", actor);
            vodList.put("vod_director", director);
            vodList.put("vod_content", desc);
            Map<String, String>vod_play = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    try {
                        int sort1 = playerConfig.getJSONObject(o1).getInt("or");
                        int sort2 = playerConfig.getJSONObject(o2).getInt("or");
                        if (sort1 == sort2) {
                            return 1;
                        }
                        return sort1 - sort2 > 0 ? 1 : -1;
                    } catch (JSONException e) {
                        SpiderDebug.log(e);
                    }
                    return 1;
                }
            });
            Elements sources = doc.select("div.stui-pannel__bd div.stui-vodlist__head h3");
            Elements sourceList = doc.select("ul.stui-content__playlist");
            for (int i = 0; i < sources.size(); i++) {
                Element source = sources.get(i);
                String sourceName = source.text();
                boolean found = false;
                for (Iterator<String>it = playerConfig.keys(); it.hasNext();) {
                    String flag = it.next();
                    if (playerConfig.getJSONObject(flag).getString("show").equals(sourceName)) {
                        sourceName = playerConfig.getJSONObject(flag).getString("show");
                        //sourceName = flag;
                        found = true;
                        break;
                    }
                }
                if (!found)
                continue;
                String playList = "";
                Elements playListA = sourceList.get(i).select("ul.stui-content__playlist > li > a");
                List<String>vodItems = new ArrayList<>();
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

    @Override
    public String playerContent(String flag, String id, List<String>vipFlags) {
        try {
            JSONObject headers = new JSONObject();
            //headers.put("Origin", "https://api.goodjson.top");
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
            headers.put("Accept", "*/*");
            headers.put("Connection", "close");
            String url = SiteUrl + "/play/id-" + id + ".html";
            Document doc = Jsoup.parse(OkHttpUtil.string(url, getHeaders(url)));
            Elements allScript = doc.select("script");
            JSONObject result = new JSONObject();
            for (int i = 0; i < allScript.size(); i++) {
                String scContent = allScript.get(i).html().trim();
                if (scContent.startsWith("var player_")) {
                    int start = scContent.indexOf('{');
                    int end = scContent.lastIndexOf('}') + 1;
                    String json = scContent.substring(start, end);
                    JSONObject player = new JSONObject(json);
                    JSONObject pCfg = playerConfig.getJSONObject(player.getString("from"));
                    if (playerConfig.has(player.getString("from"))) {
                        HashMap<String, String>hashMap = new HashMap<>();
                        hashMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
                        hashMap.put("Referer", url);
                        String jxurl = pCfg.getString("parse") + player.getString("url") + "&next=" + player.getString("link_next") + "&id=" + player.getString("id") + "&nid=" + player.getString("nid") + "&from=" + player.getString("from");
                        String doc1 = OkHttpUtil.string(jxurl, hashMap);
                        Matcher matcher = regexUrl.matcher(doc1);
                        if (!matcher.find()) {
                            return "";
                        }
                        String data = matcher.group(0);
                        String iv = "c487ebl2e38a0faO";
                        String key = "Of84ff0clf252cba";
                        String directUrl = AES.CBC(data, key, iv);
                        result.put("parse", pCfg.getString("ps"));
                        result.put("playUrl", "");
                        result.put("url", directUrl);
                        result.put("header", headers.toString());
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
            String url = SiteUrl + "/index.php/ajax/suggest?mid=1&wd=" + URLEncoder.encode(key) + "&limit=10&timestamp=" + currentTime;
            JSONObject searchResult = new JSONObject(OkHttpUtil.string(url, getHeaders(url)));
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
