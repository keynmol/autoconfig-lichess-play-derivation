import play.api.{ConfigLoader, Configuration}

import com.typesafe.config.*
import scala.concurrent.duration.*

@main def test =
  {
    case class Foo(str: String, int: Int)

    given ConfigLoader[Foo] = AutoConfig.loader[Foo]

    val config = Configuration(ConfigFactory.parseString("""
      |foo = {
      |  str = string
      |  int = 7
      |}
    """.stripMargin))

    assert(config.get[Foo]("foo") == Foo("string", 7))
  }

  {

    final class BarApiConfig(
        @ConfigName("api-key") val apiKey: String,
        @ConfigName("api-password") val apiPassword: String,
        @ConfigName("request-timeout") val requestTimeout: Duration
    ):
      override def equals(that: Any): Boolean = that match
        case c: BarApiConfig =>
          (c.apiKey, c.apiPassword, c.requestTimeout) == ((
            this.apiKey,
            this.apiPassword,
            this.requestTimeout
          ))
        case _ =>
          false
    end BarApiConfig
    object BarApiConfig:
      given ConfigLoader[BarApiConfig] = AutoConfig.loader
      def fromConfiguration(conf: Configuration) =
        conf.get[BarApiConfig]("api.foo")

    val conf = Configuration(ConfigFactory.parseString("""
      |api.foo {
      |  api-key = "abcdef"
      |  api-password = "secret"
      |  request-timeout = 1 minute
      |}
    """.stripMargin))

    assert(BarApiConfig.fromConfiguration(conf) == {
      new BarApiConfig("abcdef", "secret", 1.minute)
    })
  }

  {

    case class FooNestedConfig(
        @ConfigName("nested.str") str: String,
        @ConfigName("nested.deep.int") int: Int
    )

    given ConfigLoader[FooNestedConfig] =
      AutoConfig.loader[FooNestedConfig]

    val config = Configuration(ConfigFactory.parseString("""
      |foo = {
      |  nested.str = string
      |  nested.deep.int = 7
      |}
    """.stripMargin))

    assert(config.get[FooNestedConfig]("foo") == {
      FooNestedConfig("string", 7)
    })
  }

  {
    case class Bar(a: String, b: String)(c: Double):
      assert(c >= 0)

    given ConfigLoader[Bar] = AutoConfig.loader
    val config = Configuration(ConfigFactory.parseString("""
          |bar = {
          |  a = hello
          |  b = goodbye
          |  c = 4.2
          |}
        """.stripMargin))

    assert(config.get[Bar]("bar") == { Bar("hello", "goodbye")(4.2) })
  }
end test
