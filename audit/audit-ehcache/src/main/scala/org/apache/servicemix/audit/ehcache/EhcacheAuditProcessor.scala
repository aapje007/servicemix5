package org.apache.servicemix.audit.ehcache

import org.apache.camel.{Processor, Exchange}
import org.slf4j.LoggerFactory
import net.sf.ehcache.{Element, CacheManager}
import scala.beans.BeanProperty
import java.lang.management.ManagementFactory
import net.sf.ehcache.management.ManagementService


class EhcacheAuditProcessor extends Processor{

  val logger = LoggerFactory.getLogger(classOf[EhcacheAuditProcessor])

  @BeanProperty
  var cacheName = "camel"


  val cacheManager = CacheManager.create(this.getClass.getResource("ehcache.xml"))
  val mBeanServer = ManagementFactory.getPlatformMBeanServer()
  ManagementService.registerMBeans(cacheManager, mBeanServer, true, true, true, true)

  cacheManager.addCache("camel")
  logger.info("!!!! EHCache cachenames:"+ cacheManager.getCacheNames())


  override def process(exchange: Exchange) = {
    val id = Option(exchange.getIn.getHeader(Exchange.BREADCRUMB_ID, classOf[String])).getOrElse(exchange.getExchangeId)
    logger.info("!!!! EHCache process:"+id+ exchange)

    persist(id, exchange)
  }

  def persist(id: String, value: Exchange) {
    CacheManager.create().getCache(cacheName).put(new Element(id, value))
  }


  def init(){

  }

  def destroy(){
     cacheManager.shutdown()
  }
}
