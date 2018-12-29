--------------------------------------------------------------------------------------------------------------------------------------------------

/*   NOTE::: This is partially completed which will return all the authors after scrapping the news article.
             Previously not worked in enviroment which require high level of Scrapping things(or First time learning the Web Scrapping).
	          So,tried my best with the help of Google of how to do it.*/

-------------------------------------------------------------------------------------------------------------------------------------------------------




package some.package;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;                                               
import com.google.gson.annotations.SerializedName;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewsScrap {

    class AuthorFilter {

        @SerializedName("ids")
        private List<Integer> mIds;

        @SerializedName("hexdigest")
        private String mDigest;

        @SerializedName("total")
        private String mTotalCount;

        @SerializedName("page")
        private int mPage;

        @SerializedName("sort")
        private String mSort;

        @SerializedName("new")
        private boolean mNew;

        public List<Integer> getIds() {
            return mIds;
        }

        public String getDigest() {
            return mDigest;
        }

        public String getTotalCount() {
            return mTotalCount;
        }

        public int getpage() {
            return mPage;
        }

        private String buildRequest() {
            String out = "total=" + mTotalCount + "&";
            out += "sort=" + mSort + "&";
            out += "page=" + mPage + "&";
            out += "new=" + mNew + "&";
            for (int i = 0; i < mIds.size(); i++) {
                out += "ids[]=" + mIds.get(i) + "&";
            }
            out += "hexdigest=" + mDigest + "&";
            return out;
        }
    }

    private static class Author {

        private String mLink;
        private String mName;
        private String mDescription;

        public Author(String name, String link, String description) {
            mLink = link;
            mName = name;
            mDescription = description;
        }

        public String getLink() {
            return mLink;
        }

        public String getName() {
            return mName;
        }

        public String getDescription() {
            return mDescription;
        }
    }
    
    private static class HtmlContainer {

        @SerializedName("html")
        private String mHtml;

        public String getHtml() {
            return mHtml;
        }
    }
    
    private static List<Author> getauthors(final AuthorFilter AuthorFilter) throws IOException {

        List<Author> authors = new ArrayList<>();

        URLConnection urlConn = new URL(" https://www.thehindu.com/archive/" + AuthorFilter.buildRequest()).openConnection();
        urlConn.setRequestProperty("User-Agent", "Mozilla");
        urlConn.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
        HtmlContainer htmlObj = new Gson().fromJson(reader, HtmlContainer.class);

        Element doc = Jsoup.parse(htmlObj.getHtml());
        Elements data = doc.select("div[data-_tn]");

        if (data.size() > 0) {
            for (int i = 2; i < data.size(); i++) {
                authors.add(new Author(data.get(i).select("a").first().attr("title"),
                        data.get(i).select("a").first().attr("href"),
                        data.get(i).select("div.pitch").first().text()));
            }

        } else {
            System.out.println("no data found");
        }
        return authors;
    }

    /**
     * Return Author filter object
     */
    private static AuthorFilter getAuthorFilter(final String filter, final int page) throws IOException {

        String response = Jsoup.connect("https://angel.co/Author_filters/search_data")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("X-Requested-With", "XMLHttpRequest")
                .data("filter_data[Author_types][]=", filter)
                .data("sort", "signal")
                .data("page", String.valueOf(page))
                .userAgent("Mozilla")
                .ignoreContentType(true)
                .post().body().text();

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return gson.fromJson(response, AuthorFilter.class);
    }

    public static void main(String[] args) throws IOException {

        int pageCount = 1;
        List<Author> authors = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            System.out.println("get page n°" + pageCount);
            AuthorFilter AuthorFilter = getAuthorFilter("Author", pageCount);
            pageCount++;
            System.out.println("digest     : " + AuthorFilter.getDigest());
            System.out.println("count      : " + AuthorFilter.getTotalCount());
            System.out.println("array size : " + AuthorFilter.getIds().size());
            System.out.println("page       : " + AuthorFilter.getpage());

            authors.addAll(getauthors(AuthorFilter));

            if (authors.size() == 0) {
                break;
            } else {
                System.out.println("size     : " + authors.size());
            }
        }
    }
}
