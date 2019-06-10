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

package com.ibm.couchdb.api.builders

import com.ibm.couchdb._
import com.ibm.couchdb.core.Client
import upickle.default.Aliases.{R, W}
import upickle.default.write

import scala.reflect.ClassTag

sealed trait DocsInResult
abstract class IncludeDocs[D: R] extends DocsInResult
trait ExcludeDocs extends DocsInResult

sealed trait MissingIdsInQuery
trait MissingAllowed extends MissingIdsInQuery
trait MissingDisallowed extends MissingIdsInQuery

sealed trait DocType
abstract class ForDocType[D: R, K: R, V: R] extends DocType
trait AnyDocType extends DocType

case class GetManyDocumentsQueryBuilder[ID <: DocsInResult, AM <: MissingIdsInQuery,
BT <: DocType] private(
    client: Client,
    db: String,
    typeMappings: TypeMapping,
    params: Map[String, String] = Map.empty[String, String],
    ids: Seq[String] = Seq.empty, view: Option[CouchView] = None) {

  private[builders] val url: String = s"/$db/_all_docs"

  lazy val tempTypeFilterView: CouchView = {
    CouchView(
      map =
          """
            |function(doc) {
            | emit([doc.kind, doc._id], doc._id);
            |}
          """.stripMargin)
  }

  def conflicts(
      conflicts: Boolean = true): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("conflicts", conflicts)
  }

  def descending(
      descending: Boolean = true): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("descending", descending)
  }

  def endKey[K: W](endKey: K): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("endkey", write(endKey))
  }

  def endKeyDocId(
      endKeyDocId: String): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("endkey_docid", endKeyDocId)
  }

  def includeDocs[D: R]: GetManyDocumentsQueryBuilder[IncludeDocs[D], AM, BT] = {
    set("include_docs", true)
  }

  def excludeDocs: GetManyDocumentsQueryBuilder[ExcludeDocs, AM, BT] = {
    set("include_docs", false)
  }

  def allowMissing: GetManyDocumentsQueryBuilder[ID, MissingAllowed, BT] = {
    setType()
  }

  def disallowMissing: GetManyDocumentsQueryBuilder[ID, MissingDisallowed, BT] = {
    setType()
  }

  def withIds(ids: Seq[String]): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set(params, ids, view)
  }

  def byType[K: R, V: R](view: String, design: String, mappedType: MappedDocType)
      (implicit kw: W[K]): ViewQueryBuilder[K, V, ID, MapOnly] = {
    new ViewQueryBuilder[K, V, ID, MapOnly](
      client, db, Option(design), Option(view), params = params).
        startKey(Tuple1(mappedType.name)).endKey(Tuple2(mappedType.name, {}))
  }

  def byType[V: R](view: String, design: String, mappedType: MappedDocType):
  ViewQueryBuilder[(String, String), V, ID, MapOnly] = {
    byType[(String, String), V](view, design, mappedType)
  }

  def byTypeUsingTemporaryView(mappedType: MappedDocType):
  ViewQueryBuilder[(String, String), String, ID, MapOnly] = {
    new ViewQueryBuilder[(String, String), String, ID, MapOnly](
      client, db, None, None, temporaryView = Option(tempTypeFilterView), params = params).
        startKey(Tuple1(mappedType.name)).endKey(Tuple2(mappedType.name, {}))
  }

  def inclusiveEnd(inclusiveEnd: Boolean = true): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("inclusive_end", inclusiveEnd)
  }

  def key[K: W](key: K): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("key", write(key))
  }

  def limit(limit: Int): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("limit", limit)
  }

  def skip(skip: Int): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("skip", skip)
  }

  def stale(stale: String): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("stale", stale)
  }

  def startKey[K: W](
      startKey: K): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("startkey", write(startKey))
  }

  def startKeyDocId(
      startKeyDocId: String): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("startkey_docid", startKeyDocId)
  }

  def updateSeq(
      updateSeq: Boolean = true): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set("update_seq", updateSeq)
  }

  private def setType[I <: DocsInResult, A <: MissingIdsInQuery, B <: DocType]():
  GetManyDocumentsQueryBuilder[I, A, B] = {
    set[I, A, B](params, ids, view)
  }

  private def set[I <: DocsInResult, A <: MissingIdsInQuery, B <: DocType]
  (_params: Map[String, String], _ids: Seq[String], _view: Option[CouchView]):
  GetManyDocumentsQueryBuilder[I, A, B] = {
    new GetManyDocumentsQueryBuilder(client, db, typeMappings, _params, _ids, _view)
  }

  private def set[I <: DocsInResult, A <: MissingIdsInQuery, B <: DocType](
      key: String, value: String): GetManyDocumentsQueryBuilder[I, A, B] = {
    set(params.updated(key, value), ids, view)
  }

  private def set[I <: DocsInResult, A <: MissingIdsInQuery, B <: DocType](
      key: String, value: Any): GetManyDocumentsQueryBuilder[I, A, B] = {
    set(key, value.toString)
  }
}

object GetManyDocumentsQueryBuilder {

  private type MDBuilder[ID <: DocsInResult, AM <: MissingIdsInQuery, BT <: DocType] =
  GetManyDocumentsQueryBuilder[ID, AM, BT]

  case class Builder[T: R, ID <: DocsInResult, AM <: MissingIdsInQuery]
  (builder: MDBuilder[ID, AM, AnyDocType]) {
    def build: QueryBasic[T] = QueryBasic(
      builder.client, builder.db, builder.url,
      builder.params, builder.ids)
  }

  case class ByTypeBuilder[K: R, V: R, D: R](
      builder: MDBuilder[IncludeDocs[D], MissingDisallowed, ForDocType[K, V, D]])
      (implicit tag: ClassTag[D], kw: W[K]) {
    def build: QueryByType[K, V, D] = {
      val view = builder.view.getOrElse(builder.tempTypeFilterView)
      QueryByType(builder.client, builder.db, view, builder.typeMappings)
    }
  }

  private type BasicBuilder = Builder[CouchKeyVals[String, CouchDocRev], ExcludeDocs,
      MissingDisallowed]

  private type AllowMissingBuilder = Builder[CouchKeyValsIncludesMissing[String, CouchDocRev],
      ExcludeDocs, MissingAllowed]

  private type IncludeDocsBuilder[D] = Builder[CouchDocs[String, CouchDocRev, D], IncludeDocs[D],
      MissingDisallowed]

  private type AllowMissingIncludeDocsBuilder[D] = Builder[CouchDocsIncludesMissing[String,
      CouchDocRev, D], IncludeDocs[D], MissingAllowed]

  implicit def buildBasic(builder: MDBuilder[ExcludeDocs, MissingDisallowed, AnyDocType]):
  BasicBuilder = new BasicBuilder(builder)

  implicit def buildAllowMissing(builder: MDBuilder[ExcludeDocs, MissingAllowed, AnyDocType]):
  AllowMissingBuilder = new AllowMissingBuilder(builder)

  implicit def buildIncludeDocs[D: R](
      builder: MDBuilder[IncludeDocs[D], MissingDisallowed, AnyDocType]): IncludeDocsBuilder[D] =
    new IncludeDocsBuilder(builder)

  implicit def buildIncludeDocsAllowMissing[D: R](
      builder: MDBuilder[IncludeDocs[D], MissingAllowed, AnyDocType]):
  AllowMissingIncludeDocsBuilder[D] = new AllowMissingIncludeDocsBuilder(builder)

  implicit def buildByTypeIncludeDocs[K: R, V: R, D: R](
      builder: MDBuilder[IncludeDocs[D], MissingDisallowed, ForDocType[K, V, D]])
      (implicit tag: ClassTag[D], kw: W[K]): ByTypeBuilder[K, V, D] = {
    ByTypeBuilder(builder)
  }

  def apply(client: Client, db: String, typeMapping: TypeMapping):
  MDBuilder[ExcludeDocs, MissingDisallowed, AnyDocType] =
    new MDBuilder(client, db, typeMapping)
}
