package org.example.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

    static final String CLIENT_ID = "chen";//先寫死帳號，外部(客戶)帳號明碼
    static final String CLIENT_SECRET = "fstop2022"; //先寫死SECRET，外部(客戶)明碼
    static final String GRANT_TYPE_PASSWORD = "password";
    static final String GRANT_TYPE_CODE = "authorization_code";
    static final String GRANT_TYPE_REFRESH = "refresh_token";
    static final String SCOPE_READ = "read";
    static final String SCOPE_WRITE = "write";
    static final int ACCESS_TOKEN_VALIDITY_SECONDS = 1 * 60 * 60;
    static final int REFRESH_TOKEN_VALIDITY_SECONDS = 6 * 60 * 60;


    private static final Logger log = LoggerFactory.getLogger(AuthorizationConfig.class);

//    /**
//     * token驗證的話，就需要注入TokenServices
//     */
//    @Autowired
//    private TokenService tokenService;

    @Autowired
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 負責從資料庫存取用戶資料，用戶資料包含密碼，用來判斷前端傳入的用戶名稱與密碼是否正確
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 用戶認證Manager,其實就是設定取出來的Token裡面要帶甚麼用戶資料，所以除了Token還可以另外再加東西，另外能如何存取路徑也是這邊負責
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 儲存Jwt Token
     */
    @Bean
    public JwtTokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * Jwt token的產生轉換器 會產生Token並轉換成Jwt
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey("systex2022");  //讀取資源(對稱密鑰)->公鑰解密驗證
        return jwtAccessTokenConverter;
    }


    /**
     * RSA key  keypair
     */
//    @Bean
//    public JWKSource jwkSource(KeyPair keyPair) {
//        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//
//        RSAKey rsaKey = new RSAKey.Builder(publicKey)
//                .privateKey(privateKey)
//                .keyID(UUID.randomUUID().toString())
//                .build();
//
//        JWKSet jwkSet = new JWKSet(rsaKey);
//
//        return new ImmutableJWKSet<>(jwkSet);
//    }
//
//    @Bean
//    public JwtDecoder jwtDecoder(KeyPair keyPair) {
//        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
//    }
//
//    @Bean
//    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
//    KeyPair generateRsaKey() {
//        KeyPair keyPair;
//        try {
//            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//            keyPairGenerator.initialize(2048);
//            keyPair = keyPairGenerator.generateKeyPair();
//        } catch (Exception ex) {
//            throw new IllegalStateException(ex);
//        }
//        return keyPair;
//    }


    /**
     * 授權伺服器端點的 非安全性配置(請求到TokenEndpoint)
     * 設定授權(athorization)及Token的Request端點(endpoints)及TokenServices
     * 注入UserDetailsSerivce 的使用者資訊，確認使用者資料
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)  //refresh_token需要userDetailsService
                .tokenStore(tokenStore())  // token的儲存方式
                .accessTokenConverter(jwtAccessTokenConverter())  // 配置JwtAccessToken转换器
                .reuseRefreshTokens(false); // 非重複使用的refreshtoken

//                .tokenEnhancer(jwtAccessTokenConverter())   //增加token資訊
//                .authorizationCodeServices(authorizationCodeServices()) //  /oauth/authorize 增加授權認證服務
//                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST); //允許Token端末請求方式
    }


    /**
     * 安全檢查流程設定，用來設定Token Endpoint的安全性與Request權限
     * 設置 /oauth/check_token 端點，通過驗證則可以打請求，其實就是.checkTokenAccess這個方法就自帶路徑了
     * 認證，指的是使用 client-id + client-secret 進行客户端認證，另外還有"用戶端認證"。意思就是有兩組帳號密碼，一個內一個外，一般User知道外面的密碼
     * 也就是使用者帳密，當輸入使用者帳密後，會打請求，並去比對內部帳號密碼(這組帳號密碼應該是設定在遠端主機或DB上面)，當兩個都符合時，才會正確可以存取，Oauth
     * 因為另外可以搭配各種路徑管制加上Token存取，即使使用者帳號密碼真的外洩、加上路徑也外洩，也能在遠端主機端上避免被竊取資料(因為你不能知道這組使用者帳號密碼可以去
     * 哪一個相符合的主機登入，即客戶端認證與用戶端認證無法吻合)
     * 其中，/oauth/check_token 端點對應 CheckTokenEndpoint 類別，用於驗證Request的Token的有效性。
     * 在客户端向资源伺服器(Resourece Service)提出請求時，會在Request的Header中帶上Token(payload?)。
     * 在资源伺服器(Resourece Service)收到客户端的Request時，會使用其中的Token，找授權伺服器確認該Token的有效性。
     * 授權伺服器端點的 安全性配置（請求到 TokenEndpoint 之前）
     * 用來配置Token Endpoint的安全约束.
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
//                .passwordEncoder(passwordEncoder) //開啟表單認證，讓Client的CrendentialsTokenEndointpFilter對請求oauth/token的時候攔截，並且對client_id與sercet進行認證
                //但會一直驗不過...先不要打開
                .allowFormAuthenticationForClients()   //同意表單驗證。針對oauth/token端點
                .checkTokenAccess("isAuthenticated()")  //開啟  /oauth/check_token驗證接口認證權限，意思就是isAuthenticated()的請求就可以打Request
                .tokenKeyAccess("permitAll()");  //開啟  /oauth/token_key驗證街口的權限，意思就是Token可以通過的條件，設定permitAll()就是全部的Token都能過
    }

    /**
     * 用來配置客户端詳細資料（ClientDetailsService）
     * 補充：為什麼要建立 Client 的 client-id 和 client-secret ？
     * 通過 client-id 編號和 client-secret，授權伺服器可以知道調用的來源以及正確性。
     * 如果有心人士拿到 Access Token ，但是没有 client-id 編號和 client-secret，也不能驗證過授權伺服器。
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients // 因為要做ResourceService，所以改用客製化方式做JWT與認證
//                .withClientDetails(userDetailsService.loadUserByUsername());   //從DB存取客戶資料，兩個方法應該可以同時寫，視需求
        clients
                .inMemory()
                .withClient(CLIENT_ID)
                .secret(CLIENT_SECRET) //todo passwordencode
                .authorities("chen") //這邊先加上這個看看
                .resourceIds("test1") //加上遠端ResourceId
                .scopes(SCOPE_READ) //遠端授權的先改成all
                .autoApprove(false) //表示需要手動授權,若為true則為自動授權(就不會跳那個要按授權的畫面)
                .authorizedGrantTypes(GRANT_TYPE_CODE, GRANT_TYPE_REFRESH) //這邊的授權模式可以接受同時多種類(共有4種)
                .accessTokenValiditySeconds(ACCESS_TOKEN_VALIDITY_SECONDS)
                .refreshTokenValiditySeconds(REFRESH_TOKEN_VALIDITY_SECONDS)
                .redirectUris("http://localhost:8082/hello");
//                .redirectUris("https://www.google.com/");
    }
}

//    ==================================================================================================================


/**
 * generate RSA key
 */
//    @Bean
//    public JWKSource<SecurityContext> jwkSource() {
//        RSAKey rsaKey = generateRsa();
//        JWKSet jwkSet = new JWKSet(rsaKey);
//        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
//    }
//
//    private static RSAKey generateRsa() {
//        KeyPair keyPair = generateRsaKey();
//        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//        return new RSAKey.Builder(publicKey)
//                .privateKey(privateKey)
//                .keyID(UUID.randomUUID().toString())
//                .build();
//    }
//
//    private static KeyPair generateRsaKey() {
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(2048);
//        return keyPairGenerator.generateKeyPair();
//    }