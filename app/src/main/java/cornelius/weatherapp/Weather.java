/*
	Authors: Cornelius Donley & Tessa O.
	File: Weather.java
	Description: Basic offline weather app that
		parses XML files saved from weather.gov
	Date: 4/20/2015
*/

package cornelius.weatherapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class Weather extends ActionBarActivity {

    Map weather = new HashMap<String, String>();
    RadioButton metric;
    RadioButton imperial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        final EditText zip = (EditText)findViewById(R.id.zipCode);
        final Button btn = (Button)findViewById(R.id.goButton);
        metric = (RadioButton) findViewById(R.id.metricButton);
        imperial = (RadioButton) findViewById(R.id.imperialButton);

        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    XmlPullParserFactory pullParserFactory;
                    pullParserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = pullParserFactory.newPullParser();
                    String filename = zip.getText() + ".xml";
                    InputStream in = getAssets().open(filename);
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(in, null);
                    parser.next();
                    parseXML(parser);
                    display(weather);
                    in.close();
                }
                catch (XmlPullParserException ex) {}
                catch (IOException ex) {}
            }
        });

        metric.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                imperial();
            }
        });

        imperial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                metric();
            }
        });
    }

    private void metric()
    {
        final TextView temp = (TextView)findViewById(R.id.temperature);
        final TextView dew = (TextView)findViewById(R.id.dew);

        final TextView pressure = (TextView)findViewById(R.id.pressure);
        final TextView gust = (TextView)findViewById(R.id.gusts);
        final TextView wind = (TextView)findViewById(R.id.windspeed);
        final TextView visibility = (TextView)findViewById(R.id.textView12);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        temp.setText(nf.format((Double.parseDouble(weather.get("temp").toString())-32)*5/9) + "º C");
        dew.setText(nf.format((Double.parseDouble(weather.get("dew").toString())-32)*5/9) + "º C");
        pressure.setText(nf.format(Double.parseDouble(weather.get("pressure").toString())*25.4)  + " mmHg");
        gust.setText(nf.format(Double.parseDouble(weather.get("gust").toString())*1.6093)  + " kph");
        wind.setText(weather.get("direction").toString() + " @ " + nf.format(Double.parseDouble(weather.get("wind").toString())*1.6093)  + " kph");
        visibility.setText(nf.format(Double.parseDouble(weather.get("visibility").toString())*1.6093)  + " km");
    }

    private void imperial()
    {
        final TextView temp = (TextView)findViewById(R.id.temperature);
        final TextView dew = (TextView)findViewById(R.id.dew);
        final TextView pressure = (TextView)findViewById(R.id.pressure);
        final TextView gust = (TextView)findViewById(R.id.gusts);
        final TextView wind = (TextView)findViewById(R.id.windspeed);
        final TextView visibility = (TextView)findViewById(R.id.textView12);

        temp.setText(weather.get("temp").toString() + "º F");
        dew.setText(weather.get("dew").toString() + "º F");
        pressure.setText(weather.get("pressure").toString()  + " inHg");
        gust.setText(weather.get("gust").toString()  + " mph");
        wind.setText(weather.get("direction").toString() + " @ " + weather.get("wind").toString()  + " mph");
        visibility.setText(weather.get("visibility").toString()  + " mi");
    }

    private void display(Map weather)
    {
        final ImageView image = (ImageView)findViewById(R.id.picture);
        final TextView location = (TextView)findViewById(R.id.location);
        final TextView time = (TextView)findViewById(R.id.time);
        final TextView conditions = (TextView)findViewById(R.id.currentConditionsLabel);
        final TextView humidity = (TextView)findViewById(R.id.humid);

        if (weather.get("conditions").toString().equals("Fair"))
            image.setImageDrawable(getDrawable(R.drawable.fair));
        if (weather.get("conditions").toString().equals("Mostly Cloudy"))
            image.setImageDrawable(getDrawable(R.drawable.mostlycloudynight));
        if (weather.get("conditions").toString().equals("Overcast"))
            image.setImageDrawable(getDrawable(R.drawable.overcast));
        if (weather.get("conditions").toString().equals(" Heavy Rain Fog/Mist"))
            image.setImageDrawable(getDrawable(R.drawable.rain));
        location.setText(weather.get("location").toString());
        time.setText(weather.get("time").toString());
        conditions.setText(weather.get("conditions").toString());
        humidity.setText(weather.get("humidity").toString() + "%");

        imperial.setClickable(true);
        metric.setClickable(true);
        if (imperial.isChecked()) imperial();
        else metric();
    }

    private Map parseXML(XmlPullParser parser)
    {
        try
        {
            String name;
            int eventType;
            parser.nextTag();
            skip(parser);
            parser.nextTag();
            skip(parser);
            parser.nextTag();
            eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                name = parser.getName();
                if(eventType != XmlPullParser.START_TAG || name == null)
                {
                    eventType = parser.next();
                    continue;
                }

                switch(name)
                {
                    case "area-description":
                        parser.next();
                        weather.put("location", parser.getText());
                        break;
                    case "start-valid-time":
                        parser.next();
                        weather.put("time", parser.getText());
                        break;
                    case "weather-conditions":
                        if(parser.getAttributeValue(null, "weather-summary") != null)
                            weather.put("conditions", parser.getAttributeValue(null, "weather-summary"));
                        break;
                    case "temperature":
                        if(parser.getAttributeValue(null, "type").equals("apparent"))
                        {
                            parser.nextTag();
                            parser.next();
                            weather.put("temp", parser.getText());
                        }
                        else
                        {
                            parser.nextTag();
                            parser.next();
                            weather.put("dew", parser.getText());
                        }
                        parser.getEventType();
                        break;
                    case "humidity":
                        parser.nextTag();
                        parser.next();
                        weather.put("humidity", parser.getText());
                        break;
                    case "direction":
                        parser.nextTag();
                        parser.next();
                        double deg = Double.parseDouble(parser.getText());
                        String [] dirs= {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
                        String dir = dirs[Math.round((float)(deg/45) % 6)];
                        weather.put("direction", dir);
                        break;
                    case "pressure":
                        parser.nextTag();
                        parser.next();
                        weather.put("pressure", parser.getText());
                        break;
                    case "wind-speed":
                        if(parser.getAttributeValue(null, "type").equals("gust"))
                        {
                            parser.nextTag();
                            parser.next();
                            try
                            {
                                Double.parseDouble(parser.getText());
                                weather.put("gust", parser.getText());
                            }
                            catch(NumberFormatException ex)
                            {weather.put("gust", "0");}
                        }
                        else
                        {
                            parser.nextTag();
                            parser.next();
                            weather.put("wind", parser.getText());
                        }
                        parser.getEventType();
                        break;
                    case "visibility":
                        parser.next();
                        weather.put("visibility", parser.getText());
                        break;
                }
                eventType = parser.next();
            }
        }
        catch (XmlPullParserException ex) {}
        catch (IOException ex) {}
        
        return weather;
    }

    /* skip(XmlPullParser) pulled from Parsing XML Data on the Android Developers website
        http://developer.android.com/training/basics/network-ops/xml.html
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}