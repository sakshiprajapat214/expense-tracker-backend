package com.chiranjiv.expense.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.chiranjiv.expense.entity.Users;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	    @Autowired
	    private JwtTokenHelper jwtHelper;


	    
	    @Autowired private CustomUserDetailService customUserDetails;

	    @Override
	    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

//	        try {
//	            Thread.sleep(500);
//	        } catch (InterruptedException e) {
//	            throw new RuntimeException(e);
//	        }
	        //Authorization

	        String requestHeader = request.getHeader("Authorization");
	        //Bearer 2352345235sdfrsfgsdfsdf
	        String username = null;
	        String token = null;
	        if (requestHeader != null && requestHeader.startsWith("Bearer")) {
	            //looking good
	            token = requestHeader.substring(7);
	            try {

	                username = this.jwtHelper.getUsernameFromToken(token);

	            } catch (IllegalArgumentException e) {
	                logger.info("Illegal Argument while fetching the username !!");
	                e.printStackTrace();
	            } catch (ExpiredJwtException e) {
	                logger.info("Given jwt token is expired !!");
	                e.printStackTrace();
	            } catch (MalformedJwtException e) {
	                logger.info("Some changed has done in token !! Invalid Token");
	                e.printStackTrace();
	            } catch (Exception e) {
	                e.printStackTrace();

	            }


	        } else {
	            logger.info("Invalid Header Value !! ");
	        }


	        //
	        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {


	            //fetch user detail from username
	            Users userDetails = customUserDetails.loadUserByUsername(username);
	            Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);
	            if (validateToken) {

	                //set the authentication
	                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                SecurityContextHolder.getContext().setAuthentication(authentication);


	            } else {
	                logger.info("Validation fails !!");
	            }


	        }

	        filterChain.doFilter(request, response);


	    }

}