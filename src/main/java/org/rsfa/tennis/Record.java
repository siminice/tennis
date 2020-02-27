package org.rsfa.tennis;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Record {
  public static void main(String[] args) {
    //File input = new File(args[0]);
    lastname = args[0];
    if (lastname.equals("*") || lastname.equals("_")) lastname = "";
    if (args.length > 1) firstname = args[1];
    if (firstname.equals("*") || firstname.equals("_")) firstname = "";
    if (args.length > 2) state = args[2].toUpperCase();
    try {
      //Document doc = Jsoup.parse(input, "UTF-8", "https://www.tennisrecord.com/");
      Document doc = Jsoup.connect(baseUrl+"/adult/search.aspx")
          .timeout(180000)
          .data("lastname", lastname)
          .data("firstname", firstname)
          .get();
      extractNames(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void extractNames(Document doc) {
    Element container = doc.selectFirst(".container1000");
    Element table = container.selectFirst(".responsive14");
    Elements rows = table.select("tr");
    List<String> links = new ArrayList();
    System.out.println("  0. Quit");
    int numOptions = 0;
    for (Element row : rows) {
      Element link = row.selectFirst("a[href]");
      if (link!=null) {
        Node loc = row.childNode(3);
        Node gender = row.childNode(5);
        Node ntrp = row.childNode(7);
        String url = link.attr("href");
        String name = link.childNode(0).toString();
        String city = loc.childNodeSize() > 0 ? String.format("(%s)", loc.childNode(0).toString()) : "?";
        String sex = gender.childNodeSize() > 0 ? gender.childNode(0).toString() : "?";
        String rating = ntrp.childNode(0).toString();
        if (state.length() < 1 || (state.length() > 0 && city.contains(state))) {
          numOptions++;
          System.out.println(String.format("%3d. %-32s %s %-8s %-24s > %s",
              numOptions, name, sex, rating, city, url));
          links.add(url);
        } else{
          System.out.println(String.format("---. %-32s %s %-8s %-24s > %s", name, sex, rating, city, url));
        }
      }
    }
    System.out.print("Option: ");
    Scanner scan = new Scanner(System.in);
    int opt = scan.nextInt();
    scan.close();
    if (opt >0 && opt <= numOptions) {
      getNumericRating(links.get(opt-1));
    }
  }

  private static void getNumericRating(String path) {
    String encPath = path.replaceAll(" ", "%20");
    String fullUrl = baseUrl + encPath;
    try {
      Document doc = Jsoup.connect(fullUrl).timeout(180000).get();
      Elements containers = doc.select(".container1000");
      Element profile = containers.get(0);
      Element wrapper = profile.selectFirst(".wrapper65");
      Element table = wrapper.selectFirst(".responsive14");
      Elements rows = table.select("tr");
      Element ratingRow = rows.get(2);
      Elements cols = ratingRow.select("td");
      Element numericRating = cols.get(1);
      Node rating = numericRating.selectFirst("span");
      System.out.println("Rating: " + rating.childNode(0).toString());

      Element results = containers.get(3);
      Element rtable = results.select("tbody").get(1);
      Elements rrows = rtable.select("tr");
      Element current = rrows.get(1);
      Element previous = rrows.get(2);
      Element before = rrows.get(3);
      Element total = rrows.get(rrows.size()-1);
      getRecord(current);
      getRecord(previous);
      getRecord(before);
      getRecord(total);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void getRecord(Element row) {
    Elements cols = row.select("td");
    Node nlabel = cols.get(0).childNode(0);
    String label = nlabel.childNodeSize() > 0 ? nlabel.childNode(0).toString() : nlabel.toString();
    String win = cols.get(2).childNode(0).toString();
    String los = cols.get(3).childNode(0).toString();
    System.out.println(String.format("%5s: %2s - %2s", label, win, los));
  }

  private static String firstname = "";
  private static String lastname = "";
  private static String state = ",";
  private static final String baseUrl = "https://www.tennisrecord.com";
}