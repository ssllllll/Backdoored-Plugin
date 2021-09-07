package me.ego.ezbd.lib.fo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import me.ego.ezbd.lib.fo.collection.StrictMap;

public final class GeoAPI {
    private static final StrictMap<String, GeoAPI.GeoResponse> cache = new StrictMap();

    public static GeoAPI.GeoResponse getCountry(InetSocketAddress ip) {
        GeoAPI.GeoResponse response = new GeoAPI.GeoResponse("", "", "", "");
        if (ip == null) {
            return response;
        } else if (!ip.getHostString().equals("127.0.0.1") && !ip.getHostString().equals("0.0.0.0")) {
            if (!cache.contains(ip.toString()) && !cache.containsValue(response)) {
                try {
                    URL url = new URL("http://ip-api.com/json/" + ip.getHostName());
                    URLConnection con = url.openConnection();
                    con.setConnectTimeout(3000);
                    con.setReadTimeout(3000);
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    Throwable var5 = null;

                    try {
                        String page;
                        String input;
                        for(page = ""; (input = r.readLine()) != null; page = page + input) {
                        }

                        response = new GeoAPI.GeoResponse(getJson(page, "country"), getJson(page, "countryCode"), getJson(page, "regionName"), getJson(page, "isp"));
                        cache.put(ip.toString(), response);
                    } catch (Throwable var18) {
                        var5 = var18;
                        throw var18;
                    } finally {
                        if (r != null) {
                            if (var5 != null) {
                                try {
                                    r.close();
                                } catch (Throwable var17) {
                                    var5.addSuppressed(var17);
                                }
                            } else {
                                r.close();
                            }
                        }

                    }
                } catch (NoRouteToHostException var20) {
                } catch (SocketTimeoutException var21) {
                } catch (IOException var22) {
                    var22.printStackTrace();
                }

                return response;
            } else {
                return (GeoAPI.GeoResponse)cache.get(ip.toString());
            }
        } else {
            return new GeoAPI.GeoResponse("local", "-", "local", "-");
        }
    }

    private static String getJson(String page, String element) {
        return page.contains("\"" + element + "\":\"") ? page.split("\"" + element + "\":\"")[1].split("\",")[0] : "";
    }

    private GeoAPI() {
    }

    public static final class GeoResponse {
        private final String countryName;
        private final String countryCode;
        private final String regionName;
        private final String isp;

        public GeoResponse(String countryName, String countryCode, String regionName, String isp) {
            this.countryName = countryName;
            this.countryCode = countryCode;
            this.regionName = regionName;
            this.isp = isp;
        }

        public String getCountryName() {
            return this.countryName;
        }

        public String getCountryCode() {
            return this.countryCode;
        }

        public String getRegionName() {
            return this.regionName;
        }

        public String getIsp() {
            return this.isp;
        }
    }
}