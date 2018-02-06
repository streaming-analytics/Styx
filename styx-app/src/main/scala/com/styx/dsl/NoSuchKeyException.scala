package com.styx.dsl

case class NoSuchKeyException(key: String) extends NoSuchElementException {
}
