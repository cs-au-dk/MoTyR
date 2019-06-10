/*
 * Copyright 2016 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.couchdb.api.builders

import com.ibm.couchdb.core.Client
import com.ibm.couchdb.Req
import org.http4s.Status
import upickle.default.Aliases.{R, W}
import upickle.default._

import scalaz.concurrent.Task

case class QueryOps(client: Client) {

  def query[Q: R](
      url: String,
      params: Map[String, String]): Task[Q] = {
    client.get[Q](url, Status.Ok, params.toSeq)
  }

  def queryByIds[K: W, Q: R](
      url: String,
      ids: Seq[K],
      params: Map[String, String]): Task[Q] = {
    postQuery[Req.DocKeys[K], Q](url, Req.DocKeys(ids), params)
  }

  def postQuery[B: W, Q: R](
      url: String,
      body: B,
      params: Map[String, String]): Task[Q] = {
    client.post[B, Q](url, Status.Ok, body, params.toSeq)
  }
}
