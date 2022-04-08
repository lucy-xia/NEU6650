package SkierServer;

import SkierServer.Message.InvalidPostMessage;
import SkierServer.Message.MissingPostMessage;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.SneakyThrows;
import org.apache.commons.pool2.impl.GenericObjectPool;


@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet{
  Connection connection = null;
  GenericObjectPool<Channel> pool = null;
  Gson gson;

//  @SneakyThrows
  @SneakyThrows
  @Override
  public void init() throws ServletException {
    System.out.println("init");
    super.init();
    ChannelPooledFactory factory = new ChannelPooledFactory();
    pool = new GenericObjectPool<>(factory);
    gson = new Gson();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    System.out.println("111");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();

    PrintWriter writer = response.getWriter();
    String jsonResponse;
    System.out.println("start dealing url");

    // check if the url exist
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write(gson.toJson(new MissingPostMessage()));
      writer.close();
      return;
    }

    //Validate url path and return response status code
    String[] urlParts = urlPath.split("/");
    String requestString = request.getReader().lines().collect(Collectors.joining());
    System.out.println(requestString);
    System.out.println("11111");

    //skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
    if (!urlParts[0].equals("") || !isValidHeader(urlParts) ) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      jsonResponse = gson.toJson(new InvalidPostMessage());
    } else {
      response.setStatus(HttpServletResponse.SC_CREATED);
      jsonResponse = gson.toJson(
          //skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
          new Skier(Integer.parseInt(urlParts[7]), gson.fromJson(requestString, LiftRide.class)));

      try {
        Channel channel = pool.borrowObject();
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("34.216.176.227");
//        final Connection conn = factory.newConnection();
//        Channel channel = conn.createChannel();
        System.out.println(jsonResponse);
        channel.queueDeclare("test1", true, false, false, null);
        channel.basicPublish("", "test1", null, jsonResponse.getBytes(StandardCharsets.UTF_8));
//        channel.close();
        pool.returnObject(channel);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    writer.write(jsonResponse);
    writer.close();
    }

  private boolean isValidHeader(String[] urlPath) {

    //skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
    if(urlPath.length == 8) {
      return isNumeric(urlPath[1]) &&
          urlPath[2].equals("seasons") &&
          isNumeric(urlPath[3]) &&
          urlPath[3].length() == 4 &&
          urlPath[4].equals("days") &&
          isNumeric(urlPath[5]) &&
          Integer.parseInt(urlPath[5]) >= 1 &&
          Integer.parseInt(urlPath[5]) <= 366 &&
          urlPath[6].equals("skiers") &&
          isNumeric(urlPath[7]);
    }
    return false;
  }

  private boolean isNumeric(String s) {
    if(s == null || s.equals("")) {
      return false;
    }
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException ignored) { }
    return false;
  }

//  @Override
//  public void destroy() {
//    super.destroy();
//    try {
//      connection.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
}

