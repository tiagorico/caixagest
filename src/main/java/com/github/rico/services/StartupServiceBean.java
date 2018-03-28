package com.github.rico.services;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import static javax.ejb.ConcurrencyManagementType.BEAN;

@Singleton
@Startup
@ConcurrencyManagement(BEAN)
public class StartupServiceBean {

    @PostConstruct
    public void applicationStartup() {
        // execute the timer task
    }

}
