package com.yue.anime.hacg

import java.util.Date

import android.os.{Parcel, Parcelable}
import com.yue.anime.hacg.Common._
import org.jsoup.nodes.Element

import scala.collection.JavaConversions._
import scala.language.implicitConversions

case class Tag(name: String, url: String) extends Parcelable {
  def this(e: Element) = {
    this(e.text(), e.attr("abs:href"))
  }

  override def describeContents() = 0

  override def writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(name)
    dest.writeString(url)
  }
}

case class Comment(content: String, user: String, face: String, time: Option[Date]) extends Parcelable {
  def this(e: Element) = {
    this(e.select(".comment-content").text(),
      e.select(".fn").text(),
      e.select(".avatar").attr("abs:src"),
      e.select("time").attr("datetime"))
  }

  override def describeContents() = 0

  override def writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(content)
    dest.writeString(user)
    dest.writeString(face)
    dest.writeLong(if (time.nonEmpty) time.get.getTime else 0)
  }
}

case class Article(title: String,
                   link: String,
                   image: String,
                   content: String,
                   time: Option[Date],
                   comments: Int,
                   author: Option[Tag],
                   category: Option[Tag],
                   tags: List[Tag]) extends Parcelable {

  def this(e: Element) = {
    this(e.select("header a").text().trim,
      e.select("header a").attr("abs:href"),
      e.select(".entry-content img").attr("abs:src"),
      e.select(".entry-content p,.entry-summary p").text().trim,
      e.select("time").attr("datetime"),
      e.select("header .comments-link").text().trim match {
        case str if str.matches( """^$\d+""") => str.toInt
        case _ => 0
      },
      e.select("header .author a").take(1).map(e => new Tag(e)).headOption,
      e.select("footer .cat-links a").take(1).map(e => new Tag(e)).headOption,
      e.select("footer .tag-links a").map(o => new Tag(o)).toList)
  }


  override def describeContents() = 0

  override def writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(title)
    dest.writeString(link)
    dest.writeString(image)
    dest.writeString(content)
    dest.writeLong(if (time.nonEmpty) time.get.getTime else 0)
    dest.writeInt(comments)
    dest.writeParcelable(author.orNull, flags)
    dest.writeParcelable(category.orNull, flags)
    dest.writeParcelableArray(tags.toArray, flags)
  }
}

object Tag {

  val CREATOR: Parcelable.Creator[Tag] = new Parcelable.Creator[Tag] {
    override def createFromParcel(source: Parcel): Tag = new Tag(
      source.readString(),
      source.readString()
    )

    override def newArray(size: Int): Array[Tag] = new Array[Tag](size)
  }
}

object Comment {

  val CREATOR: Parcelable.Creator[Comment] = new Parcelable.Creator[Comment] {
    override def createFromParcel(source: Parcel): Comment = new Comment(
      source.readString(),
      source.readString(),
      source.readString(),
      source.readLong() match {
        case 0 => None
        case l => Option(new Date(l))
      }
    )

    override def newArray(size: Int): Array[Comment] = new Array[Comment](size)
  }
}

object Article {

  val CREATOR: Parcelable.Creator[Article] = new Parcelable.Creator[Article] {
    override def createFromParcel(source: Parcel): Article = new Article(
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readLong() match {
        case 0 => None
        case l => Option(new Date(l))
      },
      source.readInt(),
      Option(source.readParcelable(classOf[Tag].getClassLoader).asInstanceOf[Tag]),
      Option(source.readParcelable(classOf[Tag].getClassLoader).asInstanceOf[Tag]),
      source.readParcelableArray(classOf[Tag].getClassLoader).map(_.asInstanceOf[Tag]).toList
    )

    override def newArray(size: Int): Array[Article] = new Array[Article](size)
  }
}
