package SkierServer;

import SkierServer.Skiers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.httpclient.HttpStatus;


@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet{
  int state = 201;

  @Override
  public void init() throws ServletException {
    super.init();
    System.out.println("111");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    System.out.println("111");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String url = request.getRequestURI().toString();
    String[] urls = url.split("/");

    BufferedReader br = request.getReader();
    String body = "";
    String str = null;
    while ((str = br.readLine()) != null) {
      body += str;
    }
    JsonObject jsonObject = new Gson().fromJson(body, JsonObject.class);
    int time = jsonObject.get("time").getAsInt();
    int liftID = jsonObject.get("liftID").getAsInt();
    int waitTime = jsonObject.get("waitTime").getAsInt();

    //* 201: “Done, and created.” Generally, your POSTs return this code.
    response.setStatus(HttpStatus.SC_CREATED);
  }
}


