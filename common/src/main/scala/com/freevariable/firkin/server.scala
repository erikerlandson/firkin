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

import akka.actor._

import colossus.IOSystem
import colossus.service._
import colossus.core.ServerRef
import colossus.protocols.http._
import HttpMethod._
import UrlParsing._

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

object Firkin {
  lazy val DEBUG = sys.env.getOrElse("FIRKIN_DEBUG", "false").toLowerCase == "true"

  def start(port: Int = 4091)(implicit io: IOSystem): ServerRef = {
    import io.actorSystem.dispatcher

    val cache = io.actorSystem.actorOf(Props[KV])

    Service.serve[Http]("firkin", port){ context =>
      context.handle{ connection =>
        import connection.callbackExecutor
        
        connection.become {
          case req @ Get on Root => req.ok("")
          
          case req @ Get on Root / "cache" => {
            val cmd = KV.LIST()
            cache ! cmd
            Callback.fromFuture(cmd.promise.future).map {
              case ls: List[String] => {
                val json = ("cachedKeys" -> ls)
                req.ok(compact(render(json)))
              }
            }
          }
          
          case req @ Post on Root / "cache" => {
            if (DEBUG) {
              Console.println(req.toString)
              Console.println(req.entity)
            }
            
            val host = req.head.singleHeader("host").getOrElse("localhost:4091")
            req.entity match {
              case Some(bytes) =>
                val cmd = KV.PUT(bytes.decodeString("UTF-8"))
                cache ! cmd
                Callback.fromFuture(cmd.promise.future).map { 
                  case s: String => 
                    req.respond(HttpCodes.FOUND, "", List(("Location", s"http://$host/cache/$s")))
                }
              case None =>
                req.respond(HttpCodes.UNPROCESSABLE_ENTITY, "cowardly refusing to store empty data")
            }
          }
          
          case req @ Get on Root / "cache" / hash => {
            val cmd = KV.GET(hash)
            cache ! cmd
            Callback.fromFuture(cmd.promise.future).map{
              case Some(v) => req.ok(v.toString )
              case None => req.notFound("")
            }
          }
          
          case req => {
            Console.println(s"unrecognized:  $req")
            req.notFound("whoops")
          }
        }
      }
    }
  }
}