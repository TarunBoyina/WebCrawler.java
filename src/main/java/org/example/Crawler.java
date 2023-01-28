package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    public HashSet<String> urlHash;
    public int MAX_DEPTH = 2;
public  Connection connection;
    public Crawler() {
        //initialize urlHAsh
        connection = DatabaseConnection.getConnection();
        urlHash = new HashSet<>();
    }

    //to save web page adta into database
    public void getPageTextAndLinks(String url, int depth) {
        //if urlHash does not contain url
        if (!urlHash.contains(url)) {
            //add url to url Hashset
            if (urlHash.add(url)) {
                System.out.println(url);
            }
            try {
                //connecting to webpage and get webpage as Document objext
                Document document = Jsoup.connect(url).timeout(5000).get();
                //get text inside that doccument/webpage
                String text = document.text().length()>1000?document.text().substring(0,999):document.text();
                //get title of webpage
                String title = document.title();
                //prepare an insertion command
                PreparedStatement preparedStatement = connection.prepareStatement("Insert into pages values(?,?,?)");
                // at first "?"
                preparedStatement.setString(1,title);
                // at second "?"
                preparedStatement.setString(2,url);
                //at third"?"
                preparedStatement.setString(3,text);
                preparedStatement.executeUpdate();
                    //Increasing depth
                depth++;
                // Limiting depth
                if (depth > MAX_DEPTH) {
                    return;
                }
                //get all Links/anchor tags that are present in webpage
                Elements availableLinksOnPage = document.select("a[href]");
                //for every link cLL THIS function recursively
                for (Element currentLink : availableLinksOnPage) {
                    getPageTextAndLinks(currentLink.attr("abs:href"), depth);
                }
            } catch (IOException | SQLException ioException){
                ioException.printStackTrace();
            }
        }
    }
        public static void main (String[]args){
            Crawler crawler = new Crawler();
            crawler.getPageTextAndLinks("http://www.javatpoint.com",0);
        }
    }

