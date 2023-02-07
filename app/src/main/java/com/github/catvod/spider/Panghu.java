package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.okhttp.OKCallBack;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;

public class Panghu extends Spider {
    private static final String siteUrl = "http://www.panghuys.com";
    private static final String siteHost = "www.panghuys.com";
    private String cookie="";
    private String referer="";


    /**
     * 播放源配置
     */
    private JSONObject playerConfig;
    /**
     * 筛选配置
     */
    private JSONObject filterConfig;
    private Pattern regexCategory = Pattern.compile("/vodtype/(\\d+).html");
    private Pattern regexVid = Pattern.compile("/v/(\\d+).html");
    private Pattern regexPlay = Pattern.compile("/ph/(\\d+)-(\\d+)-(\\d+).html");
    private Pattern regexPage = Pattern.compile("\\S+/page/(\\d+)\\S+");

    @Override
    public void init(Context context) {
        super.init(context);
        try {
            playerConfig = new JSONObject("{\"qq\":{\"sh\":\"胖虎¹\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"lzm3u8\":{\"sh\":\"量子\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"1080P\":{\"sh\":\"漫专\",\"pu\":\"http://iwebs.ml/player/?url=\",\"sn\":1,\"or\":999},\"bilibili\":{\"sh\":\"缘分\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"youku\":{\"sh\":\"胖虎½\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"qiyi\":{\"sh\":\"胖虎⅓\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"iva\":{\"sh\":\"备ad¹\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"mgtv\":{\"sh\":\"PC\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"rx\":{\"sh\":\"蓝光①\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"xinluan\":{\"sh\":\"蓝光②\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"XRJX\":{\"sh\":\"蓝光③\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"hrmb\":{\"sh\":\"蓝光④\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"BYGA\":{\"sh\":\"蓝光⑤\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"LINE405\":{\"sh\":\"备选①\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"XAL\":{\"sh\":\"测试\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"sohu\":{\"sh\":\"人品₁\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"letv\":{\"sh\":\"人品₂\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999},\"wjm3u8\":{\"sh\":\"备ad²\",\"pu\":\"http://iwebs.ml/?url=\",\"sn\":1,\"or\":999}}");
            filterConfig = new JSONObject("{\"1\":[{\"key\":\"class\",\"name\":\"剧情\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"动作\",\"v\":\"动作\"},{\"n\":\"喜剧\",\"v\":\"喜剧\"},{\"n\":\"爱情\",\"v\":\"爱情\"},{\"n\":\"科幻\",\"v\":\"科幻\"},{\"n\":\"剧情\",\"v\":\"剧情\"},{\"n\":\"悬疑\",\"v\":\"悬疑\"},{\"n\":\"惊悚\",\"v\":\"惊悚\"},{\"n\":\"恐怖\",\"v\":\"恐怖\"},{\"n\":\"犯罪\",\"v\":\"犯罪\"},{\"n\":\"谍战\",\"v\":\"谍战\"},{\"n\":\"冒险\",\"v\":\"冒险\"},{\"n\":\"奇幻\",\"v\":\"奇幻\"},{\"n\":\"灾难\",\"v\":\"灾难\"},{\"n\":\"战争\",\"v\":\"战争\"},{\"n\":\"动画\",\"v\":\"动画\"},{\"n\":\"歌舞\",\"v\":\"歌舞\"},{\"n\":\"历史\",\"v\":\"历史\"},{\"n\":\"传记\",\"v\":\"传记\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"area\",\"name\":\"地区\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"中国大陆\",\"v\":\"中国大陆\"},{\"n\":\"中国香港\",\"v\":\"中国香港\"},{\"n\":\"中国台湾\",\"v\":\"中国台湾\"},{\"n\":\"韩国\",\"v\":\"韩国\"},{\"n\":\"日本\",\"v\":\"日本\"},{\"n\":\"美国\",\"v\":\"美国\"},{\"n\":\"泰国\",\"v\":\"泰国\"},{\"n\":\"新加坡\",\"v\":\"新加坡\"},{\"n\":\"马来西亚\",\"v\":\"马来西亚\"},{\"n\":\"印度\",\"v\":\"印度\"},{\"n\":\"法国\",\"v\":\"法国\"},{\"n\":\"英国\",\"v\":\"英国\"},{\"n\":\"德国\",\"v\":\"德国\"},{\"n\":\"加拿大\",\"v\":\"加拿大\"},{\"n\":\"西班牙\",\"v\":\"西班牙\"},{\"n\":\"俄罗斯\",\"v\":\"俄罗斯\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"lang\",\"name\":\"语言\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"普通话\",\"v\":\"普通话\"},{\"n\":\"英语\",\"v\":\"英语\"},{\"n\":\"韩语\",\"v\":\"韩语\"},{\"n\":\"日语\",\"v\":\"日语\"},{\"n\":\"法语\",\"v\":\"法语\"},{\"n\":\"泰语\",\"v\":\"泰语\"},{\"n\":\"德语\",\"v\":\"德语\"},{\"n\":\"印度语\",\"v\":\"印度语\"},{\"n\":\"国语\",\"v\":\"国语\"},{\"n\":\"粤语\",\"v\":\"粤语\"},{\"n\":\"俄语\",\"v\":\"俄语\"},{\"n\":\"西班牙语\",\"v\":\"西班牙语\"},{\"n\":\"意大利语\",\"v\":\"意大利语\"},{\"n\":\"其它\",\"v\":\"其它\"}]},{\"key\":\"year\",\"name\":\"年份\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"2022\",\"v\":\"2022\"},{\"n\":\"2021\",\"v\":\"2021\"},{\"n\":\"2020\",\"v\":\"2020\"},{\"n\":\"2019\",\"v\":\"2019\"},{\"n\":\"2018\",\"v\":\"2018\"},{\"n\":\"2017\",\"v\":\"2017\"},{\"n\":\"2016\",\"v\":\"2016\"},{\"n\":\"2015\",\"v\":\"2015\"},{\"n\":\"2014\",\"v\":\"2014\"},{\"n\":\"2013\",\"v\":\"2013\"},{\"n\":\"2012\",\"v\":\"2012\"},{\"n\":\"2011\",\"v\":\"2011\"},{\"n\":\"2010\",\"v\":\"2010\"},{\"n\":\"2009\",\"v\":\"2009\"},{\"n\":\"2008\",\"v\":\"2008\"},{\"n\":\"2007\",\"v\":\"2007\"},{\"n\":\"2006\",\"v\":\"2006\"},{\"n\":\"2005\",\"v\":\"2005\"},{\"n\":\"2004\",\"v\":\"2004\"},{\"n\":\"2003\",\"v\":\"2003\"},{\"n\":\"2002\",\"v\":\"2002\"},{\"n\":\"2001\",\"v\":\"2001\"},{\"n\":\"2000\",\"v\":\"2000\"}]},{\"key\":\"letter\",\"name\":\"字母\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"A\",\"v\":\"A\"},{\"n\":\"B\",\"v\":\"B\"},{\"n\":\"C\",\"v\":\"C\"},{\"n\":\"D\",\"v\":\"D\"},{\"n\":\"E\",\"v\":\"E\"},{\"n\":\"F\",\"v\":\"F\"},{\"n\":\"G\",\"v\":\"G\"},{\"n\":\"H\",\"v\":\"H\"},{\"n\":\"I\",\"v\":\"I\"},{\"n\":\"J\",\"v\":\"J\"},{\"n\":\"K\",\"v\":\"K\"},{\"n\":\"L\",\"v\":\"L\"},{\"n\":\"M\",\"v\":\"M\"},{\"n\":\"N\",\"v\":\"N\"},{\"n\":\"O\",\"v\":\"O\"},{\"n\":\"P\",\"v\":\"P\"},{\"n\":\"Q\",\"v\":\"Q\"},{\"n\":\"R\",\"v\":\"R\"},{\"n\":\"S\",\"v\":\"S\"},{\"n\":\"T\",\"v\":\"T\"},{\"n\":\"U\",\"v\":\"U\"},{\"n\":\"V\",\"v\":\"V\"},{\"n\":\"W\",\"v\":\"W\"},{\"n\":\"X\",\"v\":\"X\"},{\"n\":\"Y\",\"v\":\"Y\"},{\"n\":\"Z\",\"v\":\"Z\"},{\"n\":\"0-9\",\"v\":\"0-9\"}]},{\"key\":\"by\",\"name\":\"排序\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"时间\",\"v\":\"time\"},{\"n\":\"人气\",\"v\":\"hits\"},{\"n\":\"评分\",\"v\":\"score\"}]}],\"2\":[{\"key\":\"class\",\"name\":\"剧情\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"科幻\",\"v\":\"科幻\"},{\"n\":\"喜剧\",\"v\":\"喜剧\"},{\"n\":\"爱情\",\"v\":\"爱情\"},{\"n\":\"剧情\",\"v\":\"剧情\"},{\"n\":\"悬疑\",\"v\":\"悬疑\"},{\"n\":\"武侠\",\"v\":\"武侠\"},{\"n\":\"青春\",\"v\":\"青春\"},{\"n\":\"偶像\",\"v\":\"偶像\"},{\"n\":\"家庭\",\"v\":\"家庭\"},{\"n\":\"奇幻\",\"v\":\"奇幻\"},{\"n\":\"剧情\",\"v\":\"剧情\"},{\"n\":\"历史\",\"v\":\"历史\"},{\"n\":\"经典\",\"v\":\"经典\"},{\"n\":\"乡村\",\"v\":\"乡村\"},{\"n\":\"都市\",\"v\":\"都市\"},{\"n\":\"古装\",\"v\":\"古装\"},{\"n\":\"历史\",\"v\":\"历史\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"area\",\"name\":\"地区\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"大陆\",\"v\":\"大陆\"},{\"n\":\"中国大陆\",\"v\":\"中国大陆\"},{\"n\":\"内地\",\"v\":\"内地\"},{\"n\":\"韩剧\",\"v\":\"韩剧\"},{\"n\":\"日本\",\"v\":\"日本\"},{\"n\":\"美国\",\"v\":\"美国\"},{\"n\":\"香港\",\"v\":\"香港\"},{\"n\":\"台湾\",\"v\":\"台湾\"},{\"n\":\"泰国\",\"v\":\"泰国\"},{\"n\":\"英国\",\"v\":\"英国\"},{\"n\":\"新加坡\",\"v\":\"新加坡\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"lang\",\"name\":\"语言\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"普通话\",\"v\":\"普通话\"},{\"n\":\"英语\",\"v\":\"英语\"},{\"n\":\"韩语\",\"v\":\"韩语\"},{\"n\":\"日语\",\"v\":\"日语\"},{\"n\":\"法语\",\"v\":\"法语\"},{\"n\":\"泰语\",\"v\":\"泰语\"},{\"n\":\"德语\",\"v\":\"德语\"},{\"n\":\"印度语\",\"v\":\"印度语\"},{\"n\":\"国语\",\"v\":\"国语\"},{\"n\":\"粤语\",\"v\":\"粤语\"},{\"n\":\"俄语\",\"v\":\"俄语\"},{\"n\":\"西班牙语\",\"v\":\"西班牙语\"},{\"n\":\"意大利语\",\"v\":\"意大利语\"},{\"n\":\"其它\",\"v\":\"其它\"}]},{\"key\":\"year\",\"name\":\"年份\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"2022\",\"v\":\"2022\"},{\"n\":\"2021\",\"v\":\"2021\"},{\"n\":\"2020\",\"v\":\"2020\"},{\"n\":\"2019\",\"v\":\"2019\"},{\"n\":\"2018\",\"v\":\"2018\"},{\"n\":\"2017\",\"v\":\"2017\"},{\"n\":\"2016\",\"v\":\"2016\"},{\"n\":\"2015\",\"v\":\"2015\"},{\"n\":\"2014\",\"v\":\"2014\"},{\"n\":\"2013\",\"v\":\"2013\"},{\"n\":\"2012\",\"v\":\"2012\"},{\"n\":\"2011\",\"v\":\"2011\"},{\"n\":\"2010\",\"v\":\"2010\"},{\"n\":\"2009\",\"v\":\"2009\"},{\"n\":\"2008\",\"v\":\"2008\"},{\"n\":\"2007\",\"v\":\"2007\"},{\"n\":\"2006\",\"v\":\"2006\"},{\"n\":\"2005\",\"v\":\"2005\"},{\"n\":\"2004\",\"v\":\"2004\"},{\"n\":\"2003\",\"v\":\"2003\"},{\"n\":\"2002\",\"v\":\"2002\"},{\"n\":\"2001\",\"v\":\"2001\"},{\"n\":\"2000\",\"v\":\"2000\"}]},{\"key\":\"letter\",\"name\":\"字母\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"A\",\"v\":\"A\"},{\"n\":\"B\",\"v\":\"B\"},{\"n\":\"C\",\"v\":\"C\"},{\"n\":\"D\",\"v\":\"D\"},{\"n\":\"E\",\"v\":\"E\"},{\"n\":\"F\",\"v\":\"F\"},{\"n\":\"G\",\"v\":\"G\"},{\"n\":\"H\",\"v\":\"H\"},{\"n\":\"I\",\"v\":\"I\"},{\"n\":\"J\",\"v\":\"J\"},{\"n\":\"K\",\"v\":\"K\"},{\"n\":\"L\",\"v\":\"L\"},{\"n\":\"M\",\"v\":\"M\"},{\"n\":\"N\",\"v\":\"N\"},{\"n\":\"O\",\"v\":\"O\"},{\"n\":\"P\",\"v\":\"P\"},{\"n\":\"Q\",\"v\":\"Q\"},{\"n\":\"R\",\"v\":\"R\"},{\"n\":\"S\",\"v\":\"S\"},{\"n\":\"T\",\"v\":\"T\"},{\"n\":\"U\",\"v\":\"U\"},{\"n\":\"V\",\"v\":\"V\"},{\"n\":\"W\",\"v\":\"W\"},{\"n\":\"X\",\"v\":\"X\"},{\"n\":\"Y\",\"v\":\"Y\"},{\"n\":\"Z\",\"v\":\"Z\"},{\"n\":\"0-9\",\"v\":\"0-9\"}]},{\"key\":\"by\",\"name\":\"排序\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"时间\",\"v\":\"time\"},{\"n\":\"人气\",\"v\":\"hits\"},{\"n\":\"评分\",\"v\":\"score\"}]}],\"4\":[{\"key\":\"class\",\"name\":\"剧情\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"动作\",\"v\":\"动作\"},{\"n\":\"热血\",\"v\":\"热血\"},{\"n\":\"科幻\",\"v\":\"科幻\"},{\"n\":\"搞笑\",\"v\":\"搞笑\"},{\"n\":\"冒险\",\"v\":\"冒险\"},{\"n\":\"推理\",\"v\":\"推理\"},{\"n\":\"机战\",\"v\":\"机战\"},{\"n\":\"校园\",\"v\":\"校园\"},{\"n\":\"情感\",\"v\":\"情感\"},{\"n\":\"萝莉\",\"v\":\"萝莉\"},{\"n\":\"运动\",\"v\":\"运动\"},{\"n\":\"战争\",\"v\":\"战争\"},{\"n\":\"少年\",\"v\":\"少年\"},{\"n\":\"少女\",\"v\":\"少女\"},{\"n\":\"社会\",\"v\":\"社会\"},{\"n\":\"原创\",\"v\":\"原创\"},{\"n\":\"亲子\",\"v\":\"亲子\"},{\"n\":\"益智\",\"v\":\"益智\"},{\"n\":\"励志\",\"v\":\"励志\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"area\",\"name\":\"地区\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"中国大陆\",\"v\":\"中国大陆\"},{\"n\":\"日本\",\"v\":\"日本\"},{\"n\":\"美国\",\"v\":\"美国\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"lang\",\"name\":\"语言\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"普通话\",\"v\":\"普通话\"},{\"n\":\"英语\",\"v\":\"英语\"},{\"n\":\"韩语\",\"v\":\"韩语\"},{\"n\":\"日语\",\"v\":\"日语\"},{\"n\":\"英语\",\"v\":\"英语\"},{\"n\":\"闽南语\",\"v\":\"闽南语\"},{\"n\":\"粤语\",\"v\":\"粤语\"},{\"n\":\"其它\",\"v\":\"其它\"}]},{\"key\":\"year\",\"name\":\"年份\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"2022\",\"v\":\"2022\"},{\"n\":\"2021\",\"v\":\"2021\"},{\"n\":\"2020\",\"v\":\"2020\"},{\"n\":\"2019\",\"v\":\"2019\"},{\"n\":\"2018\",\"v\":\"2018\"},{\"n\":\"2017\",\"v\":\"2017\"},{\"n\":\"2016\",\"v\":\"2016\"},{\"n\":\"2015\",\"v\":\"2015\"},{\"n\":\"2014\",\"v\":\"2014\"},{\"n\":\"2013\",\"v\":\"2013\"},{\"n\":\"2012\",\"v\":\"2012\"},{\"n\":\"2011\",\"v\":\"2011\"},{\"n\":\"2010\",\"v\":\"2010\"},{\"n\":\"2009\",\"v\":\"2009\"},{\"n\":\"2008\",\"v\":\"2008\"},{\"n\":\"2007\",\"v\":\"2007\"},{\"n\":\"2006\",\"v\":\"2006\"},{\"n\":\"2005\",\"v\":\"2005\"},{\"n\":\"2004\",\"v\":\"2004\"},{\"n\":\"2003\",\"v\":\"2003\"},{\"n\":\"2002\",\"v\":\"2002\"},{\"n\":\"2001\",\"v\":\"2001\"},{\"n\":\"2000\",\"v\":\"2000\"}]},{\"key\":\"letter\",\"name\":\"字母\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"A\",\"v\":\"A\"},{\"n\":\"B\",\"v\":\"B\"},{\"n\":\"C\",\"v\":\"C\"},{\"n\":\"D\",\"v\":\"D\"},{\"n\":\"E\",\"v\":\"E\"},{\"n\":\"F\",\"v\":\"F\"},{\"n\":\"G\",\"v\":\"G\"},{\"n\":\"H\",\"v\":\"H\"},{\"n\":\"I\",\"v\":\"I\"},{\"n\":\"J\",\"v\":\"J\"},{\"n\":\"K\",\"v\":\"K\"},{\"n\":\"L\",\"v\":\"L\"},{\"n\":\"M\",\"v\":\"M\"},{\"n\":\"N\",\"v\":\"N\"},{\"n\":\"O\",\"v\":\"O\"},{\"n\":\"P\",\"v\":\"P\"},{\"n\":\"Q\",\"v\":\"Q\"},{\"n\":\"R\",\"v\":\"R\"},{\"n\":\"S\",\"v\":\"S\"},{\"n\":\"T\",\"v\":\"T\"},{\"n\":\"U\",\"v\":\"U\"},{\"n\":\"V\",\"v\":\"V\"},{\"n\":\"W\",\"v\":\"W\"},{\"n\":\"X\",\"v\":\"X\"},{\"n\":\"Y\",\"v\":\"Y\"},{\"n\":\"Z\",\"v\":\"Z\"},{\"n\":\"0-9\",\"v\":\"0-9\"}]},{\"key\":\"by\",\"name\":\"排序\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"时间\",\"v\":\"time\"},{\"n\":\"人气\",\"v\":\"hits\"},{\"n\":\"评分\",\"v\":\"score\"}]}],\"3\":[{\"key\":\"class\",\"name\":\"剧情\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"脱口秀\",\"v\":\"脱口秀\"},{\"n\":\"真人秀\",\"v\":\"真人秀\"},{\"n\":\"搞笑\",\"v\":\"搞笑\"},{\"n\":\"访谈\",\"v\":\"访谈\"},{\"n\":\"晚会\",\"v\":\"晚会\"},{\"n\":\"旅游\",\"v\":\"旅游\"},{\"n\":\"美食\",\"v\":\"美食\"},{\"n\":\"记实\",\"v\":\"记实\"},{\"n\":\"体育\",\"v\":\"体育\"},{\"n\":\"生活\",\"v\":\"生活\"},{\"n\":\"游戏\",\"v\":\"游戏\"},{\"n\":\"音乐\",\"v\":\"音乐\"},{\"n\":\"其他\",\"v\":\"其他\"}]},{\"key\":\"lang\",\"name\":\"语言\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"英语\",\"v\":\"英语\"},{\"n\":\"韩语\",\"v\":\"韩语\"},{\"n\":\"韩语\",\"v\":\"韩语\"},{\"n\":\"普通话\",\"v\":\"普通话\"},{\"n\":\"闽南语\",\"v\":\"闽南语\"},{\"n\":\"粤语\",\"v\":\"粤语\"},{\"n\":\"其它\",\"v\":\"其它\"}]},{\"key\":\"year\",\"name\":\"年份\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"2022\",\"v\":\"2022\"},{\"n\":\"2021\",\"v\":\"2021\"},{\"n\":\"2020\",\"v\":\"2020\"},{\"n\":\"2019\",\"v\":\"2019\"},{\"n\":\"2018\",\"v\":\"2018\"},{\"n\":\"2017\",\"v\":\"2017\"},{\"n\":\"2016\",\"v\":\"2016\"},{\"n\":\"2015\",\"v\":\"2015\"},{\"n\":\"2014\",\"v\":\"2014\"},{\"n\":\"2013\",\"v\":\"2013\"},{\"n\":\"2012\",\"v\":\"2012\"},{\"n\":\"2011\",\"v\":\"2011\"},{\"n\":\"2010\",\"v\":\"2010\"},{\"n\":\"2009\",\"v\":\"2009\"},{\"n\":\"2008\",\"v\":\"2008\"},{\"n\":\"2007\",\"v\":\"2007\"},{\"n\":\"2006\",\"v\":\"2006\"},{\"n\":\"2005\",\"v\":\"2005\"},{\"n\":\"2004\",\"v\":\"2004\"},{\"n\":\"2003\",\"v\":\"2003\"},{\"n\":\"2002\",\"v\":\"2002\"},{\"n\":\"2001\",\"v\":\"2001\"},{\"n\":\"2000\",\"v\":\"2000\"}]},{\"key\":\"letter\",\"name\":\"字母\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"A\",\"v\":\"A\"},{\"n\":\"B\",\"v\":\"B\"},{\"n\":\"C\",\"v\":\"C\"},{\"n\":\"D\",\"v\":\"D\"},{\"n\":\"E\",\"v\":\"E\"},{\"n\":\"F\",\"v\":\"F\"},{\"n\":\"G\",\"v\":\"G\"},{\"n\":\"H\",\"v\":\"H\"},{\"n\":\"I\",\"v\":\"I\"},{\"n\":\"J\",\"v\":\"J\"},{\"n\":\"K\",\"v\":\"K\"},{\"n\":\"L\",\"v\":\"L\"},{\"n\":\"M\",\"v\":\"M\"},{\"n\":\"N\",\"v\":\"N\"},{\"n\":\"O\",\"v\":\"O\"},{\"n\":\"P\",\"v\":\"P\"},{\"n\":\"Q\",\"v\":\"Q\"},{\"n\":\"R\",\"v\":\"R\"},{\"n\":\"S\",\"v\":\"S\"},{\"n\":\"T\",\"v\":\"T\"},{\"n\":\"U\",\"v\":\"U\"},{\"n\":\"V\",\"v\":\"V\"},{\"n\":\"W\",\"v\":\"W\"},{\"n\":\"X\",\"v\":\"X\"},{\"n\":\"Y\",\"v\":\"Y\"},{\"n\":\"Z\",\"v\":\"Z\"},{\"n\":\"0-9\",\"v\":\"0-9\"}]},{\"key\":\"by\",\"name\":\"排序\",\"value\":[{\"n\":\"全部\",\"v\":\"\"},{\"n\":\"时间\",\"v\":\"time\"},{\"n\":\"人气\",\"v\":\"hits\"},{\"n\":\"评分\",\"v\":\"score\"}]}]}");
        } catch (JSONException e) {
            SpiderDebug.log(e);
        }
    }

    protected static HashMap<String, String> Headers() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.62 Safari/537.36");
        headers.put("Referer", siteUrl);
        return headers;
    }

    /**
     * 爬虫headers
     *
     * @param url
     * @return
     */
    protected HashMap<String, String> getHeaders(String url) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("referer", " http://www.panghuys.com");
        headers.put("Host", siteHost);
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("DNT", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        return headers;
    }

     protected HashMap<String, String> postHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Origin", " http://iwebs.ml");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        headers.put("Accept-Encoding", " gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        return headers;
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
            Document doc = Jsoup.parse(OkHttpUtil.string(siteUrl, getHeaders(siteUrl)));
            // 分类节点
            Elements elements = doc.select("ul.navbar-items>li.navbar-item>a");
            JSONArray classes = new JSONArray();
            for (Element ele : elements) {
                String name = ele.text();
                boolean show = name.equals("电影") ||
                        name.equals("剧集") ||
                        name.equals("动漫") ||
                        name.equals("综艺");
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
                Element homeList = doc.select("div.module-main").get(0);
                Elements list = homeList.select("div.module-items>a");
                System.out.println("list..." + list);
                JSONArray videos = new JSONArray();
                for (int i = 0; i < list.size(); i++) {
                    Element vod = list.get(i);
                    String title = vod.attr("title");
                    String cover = vod.selectFirst("img.lazyload").attr("data-original");
                    String remark = vod.selectFirst("div.module-item-note").text();
                    Matcher matcher = regexVid.matcher(vod.attr("href"));
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
            String url = siteUrl + "/vodshow/";
            if (extend != null && extend.size() > 0 && extend.containsKey("tid") && extend.get("tid").length() > 0) {
                url += extend.get("tid");
            } else {
                url += tid;
            }
            if (extend != null && extend.size() > 0) {
                for (Iterator<String> it = extend.keySet().iterator(); it.hasNext(); ) {
                    String key = it.next();
                    String value = extend.get(key);
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

            // 取页码相关信息
            Elements pageInfo = doc.select("div[id='page']");
            if (pageInfo.size() == 0) {
                page = Integer.parseInt(pg);
                pageCount = page;
            } else {
                for (int i = 0; i < pageInfo.size(); i++) {
                    Element li = pageInfo.get(i);
                    Element a = li.selectFirst("a");
                    if (a == null)
                        continue;
                    String span = pageInfo.select("span.page-current").text();
                    String wy = doc.select("div[id='page'] a").last().attr("href");
                    if (page == -1) {
                        page = Integer.parseInt(span);
                    } else {
                        page = 0;
                    }
                    Matcher matcher = regexPage.matcher(wy);
                    if (matcher.find()) {
                        pageCount = Integer.parseInt(matcher.group(1).trim());
                    } else {
                        pageCount = 0;
                    }
                    break;

                }
            }

            JSONArray videos = new JSONArray();
            if (!html.contains("没有找到您想要的结果哦")) {
                Elements list = doc.select("div[class='module-items module-poster-items-base'] >a");
                for (int i = 0; i < list.size(); i++) {
                    Element vod = list.get(i);
                    String title = vod.selectFirst(".module-poster-item").attr("title");
                    String cover = vod.selectFirst("div.module-item-cover div.module-item-pic img").attr("data-original");
                    if (!TextUtils.isEmpty(cover) && !cover.startsWith("http")) {
                        cover = siteUrl + cover;
                    }
                    String remark = vod.selectFirst("div.module-item-cover div.module-item-note").text();
                    Matcher matcher = regexVid.matcher(vod.selectFirst(".module-poster-item").attr("href"));
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
            result.put("limit", 48);
            result.put("total", pageCount <= 1 ? videos.length() : pageCount * 48);

            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
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
            String url = siteUrl + "/v/" + ids.get(0) + ".html";
            Document doc = Jsoup.parse(OkHttpUtil.string(url,getHeaders2(url,referer)));
            JSONObject result = new JSONObject();
            JSONObject vodList = new JSONObject();

            // 取基本数据
            String cover = doc.selectFirst("div.module-item-cover div.module-item-pic > img").attr("data-original");
            String title = doc.selectFirst("div.module-info-main div.module-info-heading > h1").text();
            String desc = doc.selectFirst("div.module-info-item div.module-info-introduction-content > p").text().trim();


            String category = "", area = "", year = "", director = "", actor = "";
            category = doc.select("div.module-info-main div.module-info-heading div.module-info-tag-link").get(2).text();
            area = doc.select("div.module-info-main div.module-info-heading div.module-info-tag-link").get(1).text();
            year = doc.select("div.module-info-main div.module-info-heading div.module-info-tag-link").get(0).text();

            Elements span_text_muted = doc.select("div.module-info-content div.module-info-items div.module-info-item");
            for (int i = 0; i < span_text_muted.size(); i++) {
                Element text = span_text_muted.get(i);
                String info = text.select("span").text();
                if (info.contains("导演")) {
                    try {
                        director = text.select("div > a").text();
                    } catch (Exception e) {
                        director = "";
                    }
                } else if (info.contains("主演")) {
                    try {
                        actor = text.select("div > a").text();
                    } catch (Exception e) {
                        actor = "";
                    }
                }
            }

            vodList.put("vod_id", ids.get(0));
            vodList.put("vod_name", title);
            vodList.put("vod_pic", cover);
            vodList.put("type_name", category);
            vodList.put("vod_year", year);
            vodList.put("vod_area", area);
            vodList.put("vod_actor", actor);
            vodList.put("vod_director", director);
            vodList.put("vod_content", desc);
            Map<String, String> vod_play = new LinkedHashMap<>();
          /*  Map<String, String> vod_play = new TreeMap<>(new Comparator<String>() {
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
            });*/

            // 取播放列表数据
            Elements sources = doc.select("div.module-tab-items-box>div>span");
            System.out.print("sor++" + sources);
            Elements sourceList = doc.select("div.module-list>div.module-play-list");
            System.out.print("sor1++" + sourceList);
            for (int i = 0; i < sources.size(); i++) {
                Element source = sources.get(i);
                String sourceName = source.text();
                //        boolean found = false;
                boolean found = true;
                //          for (Iterator<String> it = playerConfig.keys(); it.hasNext(); ) {
                //               String flag = it.next();
                //              if (playerConfig.getJSONObject(flag).getString("sh").equals(sourceName)) {
                //                  sourceName = playerConfig.getJSONObject(flag).getString("sh");
                //                  found = true;
                //                   break;
                //               }
                //            }
                if (!found)
                    continue;
                String playList = "";
                Elements playListA = sourceList.get(i).select("div.module-play-list-content>a");
                System.out.print("pl++" + playList);
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
     private final Pattern key = Pattern.compile("(?<=key\":\\s\").*?(?=\")");
    private final Pattern time = Pattern.compile("(?<=time\":\\s\").*?(?=\")");
    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            //定义播放用的headers
            JSONObject headers = new JSONObject();
            //headers.put("Host", " cokemv.co");
            
            headers.put("User-Agent", " Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
            headers.put("Accept", " */*");
            headers.put("Accept-Language", " zh-CN,zh;q=0.9,en-US;q=0.3,en;q=0.7");
            headers.put("Accept-Encoding", " gzip, deflate");
            // 播放页 url
            String url = siteUrl + "/ph/" + id + ".html";
            Document doc = Jsoup.parse(OkHttpUtil.string(url, Headers()));
            Elements allScript = doc.select("script");
            JSONObject result = new JSONObject();
            for (int i = 0; i < allScript.size(); i++) {
                String scContent = allScript.get(i).html().trim();
                if (scContent.startsWith("var player_")) { // 取直链
                    int start = scContent.indexOf('{');
                    int end = scContent.lastIndexOf('}') + 1;
                    String json = scContent.substring(start, end);
                    JSONObject player = new JSONObject(json);
                    String fuckUrl = player.getString("url");
                    if(fuckUrl.contains("m3u8")){
                    result.put("url",fuckUrl);
                    
                    }else{
                   
                       JSONObject pCfg = playerConfig.getJSONObject(player.getString("from"));
                        
                        String jxurl = pCfg.getString("pu") + fuckUrl;
                        Document doc2 = Jsoup.parse(OkHttpUtil.string(jxurl, Headers()));
                        Elements script = doc2.select("body>script");
                        for (int j = 0; j < script.size(); j++) {
                            String Content = script.get(j).html().trim();
							Matcher matcher = time.matcher(Content);
							String video_tm = "";
							if (matcher.find())  {                       
								video_tm = matcher.group(0);
							}
							Matcher matcher1 = key.matcher(Content);
							String key = "";
							if (matcher1.find()){
								key = matcher1.group(0);
							}


							HashMap hashMap = new HashMap();
                               //     hashMap.put("token", video_token);
                                    hashMap.put("url", fuckUrl);
                                    hashMap.put("time", video_tm);
                                    hashMap.put("key", key);
                                    
                            OkHttpUtil.post(OkHttpUtil.defaultClient(), "http://iwebs.ml/api_config.php", hashMap, postHeaders(),new OKCallBack.OKCallBackString() {
                                
                                protected void onFailure(Call call, Exception exc) {
                                }

                                public void onResponse(String str) {
                                    try {
                                        String url = new JSONObject(str).getString("url");
                                        
                                        result.put("url", url);
                                        
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });}
                        }
                        result.put("parse", 0);
                        result.put("playUrl", "");
                        result.put("header", headers.toString());
                    }
                    
                }
            
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    protected HashMap<String, String> getHeaders2(String url,String ref) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        if(!ref.equals("google")){
            headers.put("Authority", "www.panghuys.com");
            if(ref.length()>0){
                if(ref.equals("origin")){
                    headers.put("Origin", "http://www.panghuys.com");
                } else {
                    headers.put("Referer", ref);
                }
            }
            if(cookie.length()>0){
                headers.put("Cookie", cookie);
            }
        }
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        return headers;
    }

    protected void getCookie(){
        cookie="";
        String cookieurl="http://www.panghuys.com/zzzzz";
        Map<String, List<String>> cookies = new HashMap<>();
        OkHttpUtil.string(cookieurl,getHeaders2(cookieurl,""),cookies);
        for( Map.Entry<String, List<String>> entry : cookies.entrySet() ){
            if(entry.getKey().equals("set-cookie")){
                cookie = TextUtils.join(";",entry.getValue());
                break;
            }
        }
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            long currentTime = System.currentTimeMillis();
            String url = siteUrl + "/index.php/ajax/suggest?mid=1&wd=" + URLEncoder.encode(key) + "&limit=35&timestamp=" + currentTime;
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