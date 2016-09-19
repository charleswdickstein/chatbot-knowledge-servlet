/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pubnub;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author josh
 */
public class JSONResponder extends HttpServlet {

    private JsonObject json;

    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        System.out.println("initting");
        InputStream stream = this.getClass().getResourceAsStream("knowledge.json");
        json = (JsonObject) Json.createReader(stream).read();
        p("read in" + json);
    }

    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        p("responding");
        String action = request.getParameter("action");
        String term = request.getParameter("term");
        p("action " + action + " term is "+ term);
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            JsonWriter writer = Json.createWriter(out);
            JsonValue obj = pick(action,term);
            if(obj.getValueType() == JsonValue.ValueType.ARRAY) {
                writer.writeArray((JsonArray) obj);
            }
            if(obj.getValueType() == JsonValue.ValueType.OBJECT) {
                writer.writeObject((JsonObject) obj);
            }
            if(obj.getValueType() == JsonValue.ValueType.STRING) {
                writer.writeObject(Json.createObjectBuilder()
                            .add("text",obj).build());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void p(String string) {
        System.out.println(string);
    }

    private JsonValue pick(String action, String term) {
        p("action is " + action);
        if("knowledge".equals(action)) {
            p("doing knowledge");
            if(json.getJsonObject("knowledge").containsKey(term)) {
                p("doing sub term");
                return json.getJsonObject("knowledge").getJsonObject(term);
            } else {
                p("doing random");
                return pickRandom(json.getJsonArray("random"));
            }            
        }
        if("favorites".equals(action)) {
            return pickRandom(json.getJsonObject("favorites").getJsonArray(term));
        }
        if(json.containsKey(action)) {
            return pickRandom(json.getJsonArray(action));
        }
        return Json.createObjectBuilder()
                .add("error", "unknown action " + action).build();
     }

    private JsonValue pickRandom(JsonArray jsonArray) {
        return jsonArray.get((int)Math.floor(Math.random()*jsonArray.size()));
    }

}
