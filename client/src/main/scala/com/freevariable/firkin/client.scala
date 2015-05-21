/*
 * Copyright (c) 2015 William C. Benton and Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.c
 */

package com.freevariable.firkin

class Client(endpointHost: String, port: Int) {
  import dispatch._
  import scala.concurrent.ExecutionContext.Implicits.global
  
  lazy val server = host(endpointHost, port)
  
  def putData(data: String) = {
    val endpoint = (server / "cache").POST
    val request = endpoint.setContentType("application/json", "UTF-8") << data
    val response = Http(request > (x => x))
    response()
  }
  
  def put(data: String): String = {
    val response = putData(data)
    val headers = response.getHeaders()
    Console.println(headers)
    response.getHeader("Location")
  }
  
  def publish(tag: String, data: String): String = {
    val response = putData(data)
    val hash = response.getHeader("X-Firkin-Hash")
    putTag(tag, hash)
  }
  
  def get(hash: String): Option[String] = {
    val endpoint = (server / "cache" / hash).GET
    val response = Http(endpoint OK as.String).option
    response()
  }
  
  def listObjects(): Option[String] = {
    val endpoint = (server / "cache").GET
    val response = Http(endpoint OK as.String).option
    response()
  }
  
  def resolveTag(tag: String): Option[String] = {
    val endpoint = (server / "tag-value" / tag).GET
    val hash = Http(endpoint OK as.String).option
    hash()
  }
  
  def getTag(tag: String): Option[String] = {
    resolveTag(tag).flatMap { h =>
      get(h)
    }
  }
  
  def putTag(tag: String, hash: String): String = {
    val endpoint = (server / "tag").POST
    val request = endpoint.setContentType("application/json", "UTF-8") << s""" {"tag" : "$tag", "hash" : "$hash"} """
    val response = Http(request > (x => x))
    response().getHeader("Location")
  }
  
  def getOrElse(hash: String, default: String): String = {
    get(hash).getOrElse(default)
  }
}