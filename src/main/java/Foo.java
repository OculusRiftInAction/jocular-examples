import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.mapsengine.MapsEngine;
import com.google.api.services.mapsengine.MapsEngine.Rasters;
import com.google.api.services.mapsengine.model.Layer;
import com.google.api.services.oauth2.Oauth2;

public class Foo {
  private static final Logger LOG = LoggerFactory.getLogger(Foo.class);
  static {
    LOG.warn("Foo");
  }
  private static final String API_KEY = "AIzaSyCaQ0F86r4k6ltqN7e6iXZmX1apyCEj1JY";
  private static final String ELEVATION_DATASET = "z4f-ZuCLmiKg.kFe7dwlM4QzE";
  private static final String APPLICATION_NAME = "JocularExamples/1.0";
  private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
      ".store/oauth2_sample");
  private static final FileDataStoreFactory DATA_STORE_FACTORY;
  private static final HttpTransport TRANSPORT;
  private static final JsonFactory JSON_FACTORY;

  private static final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile",
      "https://www.googleapis.com/auth/userinfo.email");
  static {
    try {
      DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
      TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      JSON_FACTORY = JacksonFactory.getDefaultInstance();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  // private static final List<String> SCOPES = Arrays.asList(
  // "https://www.googleapis.com/auth/userinfo.profile",
  // "https://www.googleapis.com/auth/userinfo.email");
  private static Oauth2 oauth2;
  private static GoogleClientSecrets clientSecrets;

  //
  private static Credential authorize() throws Exception {
    // load client secrets
    clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
        new InputStreamReader(Foo.class.getResourceAsStream("/client_secrets.json")));
    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/ "
          + "into oauth2-cmdline-sample/src/main/resources/client_secrets.json");
      System.exit(1);
    }
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(TRANSPORT, JSON_FACTORY, clientSecrets,
        SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  //
  public static void main(String[] args) throws Exception {
    // authorization
    // Credential credential = authorize();
    // set up global Oauth2 instance
    // oauth2 = new Oauth2.Builder(TRANSPORT, JSON_FACTORY,
    // credential).setApplicationName(
    // APPLICATION_NAME).build();
    // // run commands
    // tokenInfo(credential.getAccessToken());
    // userInfo();
    // success!
    MapsEngine.Builder builder = new MapsEngine.Builder(TRANSPORT, JSON_FACTORY, new ClientParametersAuthentication(
        "984164781569-4s4jtnmtagqm7mtbimqbqek47kds73dl.apps.googleusercontent.com", "iiJoVneLx6FwyMEMx427W7AN"));
    builder.setApplicationName("JocularExamples");
    MapsEngine maps = builder.build();
    maps.tables().
    Rasters rasters = maps.rasters();
    Layer layer = maps.layers().get(ELEVATION_DATASET).execute();
    System.out.println();
  }

  // private static void tokenInfo(String accessToken) throws IOException {
  // header("Validating a token");
  // Tokeninfo tokeninfo =
  // oauth2.tokeninfo().setAccessToken(accessToken).execute();
  // System.out.println(tokeninfo.toPrettyString());
  // if
  // (!tokeninfo.getAudience().equals(clientSecrets.getDetails().getClientId()))
  // {
  // System.err.println("ERROR: audience does not match our client ID!");
  // }
  // }
  //
  // private static void userInfo() throws IOException {
  // header("Obtaining User Profile Information");
  // Userinfoplus userinfo = oauth2.userinfo().get().execute();
  // System.out.println(userinfo.toPrettyString());
  // }
  //
  // static void header(String name) {
  // System.out.println();
  // System.out.println("================== " + name + " ==================");
  // System.out.println();
  // }
}
