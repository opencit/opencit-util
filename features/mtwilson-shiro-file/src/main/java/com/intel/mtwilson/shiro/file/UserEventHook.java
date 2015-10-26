/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.shiro.file;

/**
 *
 * @author ascrawfo
 */
public interface UserEventHook {
    void afterCreateUser(String username); 
    void afterUpdateUser(String username);
    void afterDeleteUser(String username); 
}
