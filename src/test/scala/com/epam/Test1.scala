package com.epam

import akka.http.scaladsl.model._

class Test1 extends org.scalatest.FunSuite with org.scalatest.Matchers {

  test("Good URL can be parsed") {

    Uri("ftp://ftp.is.co.za/rfc/rfc1808.txt") shouldEqual
      Uri.from(scheme = "ftp", host = "ftp.is.co.za", path = "/rfc/rfc1808.txt")

    Uri("http://www.ietf.org/rfc/rfc2396.txt") shouldEqual
      Uri.from(scheme = "http", host = "www.ietf.org", path = "/rfc/rfc2396.txt")

    Uri("ldap://[2001:db8::7]/c=GB?objectClass?one") shouldEqual
      Uri.from(scheme = "ldap", host = "[2001:db8::7]", path = "/c=GB", queryString = Some("objectClass?one"))

    Uri("mailto:John.Doe@example.com") shouldEqual
      Uri.from(scheme = "mailto", path = "John.Doe@example.com")

    Uri("news:comp.infosystems.www.servers.unix") shouldEqual
      Uri.from(scheme = "news", path = "comp.infosystems.www.servers.unix")

    Uri("tel:+1-816-555-1212") shouldEqual
      Uri.from(scheme = "tel", path = "+1-816-555-1212")

    Uri("telnet://192.0.2.16:80/") shouldEqual
      Uri.from(scheme = "telnet", host = "192.0.2.16", port = 80, path = "/")

    Uri("urn:oasis:names:specification:docbook:dtd:xml:4.1.2") shouldEqual
      Uri.from(scheme = "urn", path = "oasis:names:specification:docbook:dtd:xml:4.1.2")

  }

  test("Don't double decode") {

    Uri("%2520").path.head shouldEqual "%20"  // "%20"
    Uri("/%2F%5C").path shouldEqual Uri.Path / """/\""" // "//\"

  }

  test("Error when invalid url is passed") {

    //illegal scheme
    the[IllegalUriException] thrownBy Uri("foö:/a") shouldBe {
      IllegalUriException(
        "Illegal URI reference: Invalid input 'ö', expected scheme-char, 'EOI', '#', ':', '?', slashSegments or pchar (line 1, column 3)",
        "foö:/a\n" +
          "  ^")
    }

    // illegal userinfo
    the[IllegalUriException] thrownBy Uri("http://user:ö@host") shouldBe {
      IllegalUriException(
        "Illegal URI reference: Invalid input 'ö', expected userinfo-char, pct-encoded, '@' or port (line 1, column 13)",
        "http://user:ö@host\n" +
          "            ^")
    }

    // illegal percent-encoding
    the[IllegalUriException] thrownBy Uri("http://use%2G@host") shouldBe {
      IllegalUriException(
        "Illegal URI reference: Invalid input 'G', expected HEXDIG (line 1, column 13)",
        "http://use%2G@host\n" +
          "            ^")
    }

    // illegal percent-encoding ends with %
    the[IllegalUriException] thrownBy Uri("http://www.example.com/%CE%B8%") shouldBe {
      IllegalUriException(
        "Illegal URI reference: Unexpected end of input, expected HEXDIG (line 1, column 31)",
        "http://www.example.com/%CE%B8%\n" +
          "                              ^")
    }

    // illegal path
    the[IllegalUriException] thrownBy Uri("http://www.example.com/name with spaces/") shouldBe {
      IllegalUriException(
        "Illegal URI reference: Invalid input ' ', expected '/', 'EOI', '#', '?' or pchar (line 1, column 28)",
        "http://www.example.com/name with spaces/\n" +
          "                           ^")
    }

    // illegal path with control character
    the[IllegalUriException] thrownBy Uri("http:///with\newline") shouldBe {
      IllegalUriException(
        "Illegal URI reference: Invalid input '\\n', expected '/', 'EOI', '#', '?' or pchar (line 1, column 13)",
        "http:///with\n" +
          "            ^")
    }

  }

  def strict(queryString: String): Uri.Query = {
    //   Query("a=b") is the same as Uri("http://localhost?a=b").query()
    Uri.Query(queryString)
  }

  test("Good queries should be parsed") {

    //query component "a=b" is parsed into parameter name: "a", and value: "b"
    strict("a=b") shouldEqual ("a", "b") +: Uri.Query.Empty

    strict("") shouldEqual ("", "") +: Uri.Query.Empty
    strict("a") shouldEqual ("a", "") +: Uri.Query.Empty
    strict("a=") shouldEqual ("a", "") +: Uri.Query.Empty
    strict("a=+") shouldEqual ("a", " ") +: Uri.Query.Empty //'+' is parsed to ' '
    strict("a=%2B") shouldEqual ("a", "+") +: Uri.Query.Empty
    strict("=a") shouldEqual ("", "a") +: Uri.Query.Empty
    strict("a&") shouldEqual ("a", "") +: ("", "") +: Uri.Query.Empty
    strict("a=%62") shouldEqual ("a", "b") +: Uri.Query.Empty

    strict("a%3Db=c") shouldEqual ("a=b", "c") +: Uri.Query.Empty
    strict("a%26b=c") shouldEqual ("a&b", "c") +: Uri.Query.Empty
    strict("a%2Bb=c") shouldEqual ("a+b", "c") +: Uri.Query.Empty
    strict("a%3Bb=c") shouldEqual ("a;b", "c") +: Uri.Query.Empty

      strict("a=b%3Dc") shouldEqual ("a", "b=c") +: Uri.Query.Empty
    strict("a=b%26c") shouldEqual ("a", "b&c") +: Uri.Query.Empty
    strict("a=b%2Bc") shouldEqual ("a", "b+c") +: Uri.Query.Empty
    strict("a=b%3Bc") shouldEqual ("a", "b;c") +: Uri.Query.Empty

    strict("a+b=c") shouldEqual ("a b", "c") +: Uri.Query.Empty //'+' is parsed to ' '
    strict("a=b+c") shouldEqual ("a", "b c") +: Uri.Query.Empty //'+' is parsed to ' '

  }


}
