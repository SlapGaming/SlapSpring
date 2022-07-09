package com.telluur.slapspring.core.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String DISCORD_USER_AGENT = "Slap Bot from Spring Boot";

    /**
     * Adds a user-agent to a RequestEntity.
     * This nonsense is needed because discord/cloudflare require it,
     * even though it is out of the OAuth2 (RFC 6749) spec.
     */
    /*
    static RequestEntity<?> withUserAgent(RequestEntity<?> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHeaders());
        headers.add(HttpHeaders.USER_AGENT, DISCORD_USER_AGENT);
        return new RequestEntity<>(request.getBody(), headers, request.getMethod(), request.getUrl());
    }

     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests().antMatchers("/").permitAll();
    }

    /*
    @Override
    protected void configure(HttpSecurity security) throws Exception {
        security
                .authorizeRequests(a -> a
                        .antMatchers("/attachments/**").permitAll()
                        .antMatchers("/", "/leaderbutts", "/error").permitAll()
                        .antMatchers("/css/**", "/font/**", "/img/**", "/js/**", "/webjars/**").permitAll()
                        .antMatchers("/link", "/sign", "/result", "/not-a-member").authenticated()
                        .anyRequest().anonymous()
                        .anyRequest().permitAll()
                )

                .httpBasic().disable() //Removes standard login
                .formLogin().disable() //Removed /login http page

                .exceptionHandling(configurer ->
                        configurer.accessDeniedPage("/error")
                )

                .csrf(configurer ->
                        configurer.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                );


                .oauth2Login(configurer -> {
                    configurer.loginPage("/");
                    configurer.tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient());
                    configurer.userInfoEndpoint().userService(userService());
                    configurer.defaultSuccessUrl("/link");
                    configurer.failureHandler((request, response, exception) -> {
                        request.getSession().setAttribute("error.message", exception.getMessage());

                    });
                });



    }
    */

    /**
     * OAuth2 authorization request
     * This nonsense is needed to prepend an out of spec (RFC 6749) user-agent header.
     */
    /*
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();

        client.setRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter() {
            @Override
            public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest oauth2Request) {
                return withUserAgent(super.convert(oauth2Request));
            }
        });
        return client;
    }


     */
    /**
     * OAuth2 user info request
     * This nonsense is needed to prepend an out of spec (RFC 6749) user-agent header.
     */
    /*
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> userService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        delegate.setRequestEntityConverter(new OAuth2UserRequestEntityConverter() {
            @Override
            public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
                return withUserAgent(super.convert(userRequest));
            }
        });
        return delegate;
    }

     */

}
