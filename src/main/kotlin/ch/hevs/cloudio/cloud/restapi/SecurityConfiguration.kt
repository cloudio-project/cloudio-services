package ch.hevs.cloudio.cloud.restapi

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter


@Configuration
@EnableConfigurationProperties
class SecurityConfiguration(var customUserDetailsService: MongoCustomUserDetailsService) : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
                .authorizeRequests().anyRequest().permitAll()
                .and().httpBasic()
                .and().sessionManagement().disable()
    }

    @Throws(Exception::class)
    public override fun configure(builder: AuthenticationManagerBuilder?) {
        builder!!.userDetailsService(customUserDetailsService)
    }

}