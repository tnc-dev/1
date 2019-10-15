package my.mkt;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class ConnectToURL extends AsyncTask<String, String, String> {

    public String responseGet;
    private static ArrayList<DataList> data = new ArrayList<>();
    private Context context;

    @Override
    protected String doInBackground(String... requestURL) {
        responseGet = "";
        try {
            URL url = new URL(requestURL[0]);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                responseGet = sb.toString();

                FileOutputStream writer = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            writer = new FileOutputStream(Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    + "/Filename.xml", false);
                            byte[] buffer = responseGet.getBytes();
                            writer.write(buffer, 0, buffer.length);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        writer = new FileOutputStream(Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                + "/Filename.xml", false);
                        byte[] buffer = responseGet.getBytes();
                        writer.write(buffer, 0, buffer.length);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                  writer.flush();

                try {


                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();

                    XMLHandler handler = new XMLHandler();
                    parser.parse(new File(Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            + "/Filename.xml"), handler);

                    for (DataList employee : data) {
                        /// System.out.println(String.format("Имя сотрудника: %s, его должность: %s", employee.getName(), employee.getJob()));
                    }

                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (ProtocolException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        return null;
}

    @Override
    protected void onPostExecute(String strings) {
        super.onPostExecute(strings);



    }


private static class XMLHandler extends DefaultHandler {
    @Override
    public void startDocument() throws SAXException {
        // Тут будет логика реакции на начало документа
    }

    @Override
    public void endDocument() throws SAXException {
        // Тут будет логика реакции на конец документа
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Тут будет логика реакции на начало элемента

        if (qName.equals("file")) {
            String date = attributes.getValue("date");
            String name = attributes.getValue("name");
            String type = attributes.getValue("type");
            data.add(new DataList(date, name, type));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Тут будет логика реакции на конец элемента
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // Тут будет логика реакции на текст между элементами
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // Тут будет логика реакции на пустое пространство внутри элементов (пробелы, переносы строчек и так далее).
    }
}
}
