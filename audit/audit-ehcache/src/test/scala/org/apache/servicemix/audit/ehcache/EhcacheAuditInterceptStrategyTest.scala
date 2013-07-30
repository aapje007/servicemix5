package org.apache.servicemix.audit.ehcache

import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.camel.{Processor, Exchange}
import org.apache.camel.builder.RouteBuilder
import org.junit.Test
import org.junit.Assert._
import org.apache.servicemix.core.camel.ServiceMix

/**
 * Basic test for the {@link MongoAuditProcessor}
 */
class EhcacheAuditInterceptStrategyTest extends CamelTestSupport {

  val servicemix = ServiceMix()

  val persisted = scala.collection.mutable.Map.empty[String, Seq[Exchange]].withDefaultValue(Seq.empty[Exchange])


  servicemix.addIntercept(new EhcacheAuditProcessor() {
    override def persist(id: String, value: Exchange) : Unit = persisted.put(id, value +: persisted(id))
  })

  @Test
  def testSimplePersistence() {
    val mock = getMockEndpoint("mock:test")
    mock.expectedMessageCount(1)

    val exchange = template.send("direct:test", new Processor() {
      override def process(exchange: Exchange) = exchange.getIn.setBody("Initial body text")
    })

    val breadcrumb = exchange.getIn.getHeader(Exchange.BREADCRUMB_ID, classOf[String])

    assertMockEndpointsSatisfied()

    assertEquals("Exchange has been audited twice for the same breadcrumb id", 2, persisted(breadcrumb).size)

    for (exc <- persisted(breadcrumb)) {
      assertEquals(exchange.getExchangeId,exc.getExchangeId )
      //assertSome("Properties should be in the persistent object", ???)
      //assertSome("In message should be in the persistent object", ???)
    }

  }

  def assertSome(message: String, option: Option[AnyRef]) = option match {
    case None => fail(message)
    case _    =>
  }

  override def createRouteBuilder(): RouteBuilder = new RouteBuilder() {
    def configure() {
      from("direct:test").transform().simple("Transformed body text at ${date:now:yyyyMMdd-hhMMssSSS}").to("mock:test")
    }
  }
}
