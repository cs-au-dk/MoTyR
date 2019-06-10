/*
 * Copyright 2015 IBM Corporation
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

package com.ibm.couchdb.api

import com.ibm.couchdb.Res
import com.ibm.couchdb.core.Client
import org.http4s.Status

import scalaz.concurrent.Task

class Databases(client: Client) {

  def get(name: String): Task[Res.DbInfo] = {
    client.get[Res.DbInfo](s"/$name", Status.Ok)
  }

  def getAll: Task[Seq[String]] = {
    client.get[Seq[String]]("/_all_dbs", Status.Ok)
  }

  def create(name: String): Task[Res.Ok] = {
    client.put[Res.Ok](s"/$name", Status.Created)
  }

  def delete(name: String): Task[Res.Ok] = {
    client.delete[Res.Ok](s"/$name", Status.Ok)
  }
}
