package com.tj.springsecurityoauth2demo.config;

import com.tj.springsecurityoauth2demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("jwtTokenStore")
    private TokenStore tokenStore;

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private JwtTokenEnhancer jwtTokenEnhancer;

//    @Autowired
//    @Qualifier("redisTokenStore")
//    private TokenStore tokenStore;

    /**
     * 使用密码模式所需配置
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        //配置JWT内容增强器
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> delegates = new ArrayList<>();
        delegates.add(jwtTokenEnhancer);
        delegates.add(jwtAccessTokenConverter);
        enhancerChain.setTokenEnhancers(delegates);


       endpoints.authenticationManager(authenticationManager)
               .userDetailsService(userService)
               //配置存储令牌策略
               .tokenStore(tokenStore)
               .accessTokenConverter(jwtAccessTokenConverter)
               .tokenEnhancer(enhancerChain);
               //.tokenStore(tokenStore);

    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                //配置Client-id
                .withClient("admin")
                //配置client-secret
                .secret(passwordEncoder.encode("112233"))
                //配置访问token的有效期，秒
                .accessTokenValiditySeconds(36000)
                //配置refresh token有效期
                .refreshTokenValiditySeconds(864000)
                //配置,授权成功后，重定向跳转页面
                //.redirectUris("http://www.baidu.com")
                .redirectUris("http://localhost:8081/login")
                //自动授权
                .autoApprove(true)
                //配置申请的权限范围
                .scopes("all")
                //配置授权类型
                //.authorizedGrantTypes("authorization_code");
                //.authorizedGrantTypes("password");
                .authorizedGrantTypes("password","refresh_token","authorization_code");
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        //获取秘钥需要身份认证，使用单点登录时，必须配置
       security.tokenKeyAccess("isAuthenticated()");
    }
}
