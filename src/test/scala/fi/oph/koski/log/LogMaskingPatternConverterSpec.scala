package fi.oph.koski.log

import fi.oph.koski.TestEnvironment
import fi.oph.koski.log.LogUtils.HETU_MASK
import org.json4s.{JObject, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.json4s.jackson.JsonMethods.parse

class LogMaskingPatternConverterSpec extends AnyFreeSpec with TestEnvironment with Logging with Matchers {
  "LogMaskingPatternConverterSpec" - {
    "Ilman hetujen maskausta" - {
      "PatternLayout: %m" in {
        logger.info("Hetu: 010101-0101")
        latestOriginalMessage should equal("Hetu: 010101-0101")
      }
      "JsonTemplateLayout: LogstashJsonEventLayoutV1.json" in {
        logger.info("Hetu: 030303-0303")
        latestJsonMessage should equal("Hetu: 030303-0303")
      }
    }
    "Hetujen maskaus" - {
      "PatternLayout: %cm" in {
        logger.info("Hetu: 020202-0202")
        latestMaskedMessage should equal(s"Hetu: ${HETU_MASK}")
      }
      "JsonTemplateLayout: MaskedLogstashJsonEventLayoutV1.json" in {
        logger.info("Hetu: 040404-0404")
        latestMaskedJsonMessage should equal(s"Hetu: ${HETU_MASK}")
      }
    }
    "Viestin lyhennys" - {
      val LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam semper porta metus ultricies varius. Donec semper, felis ac condimentum fringilla, enim arcu iaculis mi, quis gravida felis augue sit amet lectus. Morbi ultrices mollis tortor at tempor. Aenean maximus eros vel tristique accumsan. Sed ac lorem lobortis diam ornare ornare a sit amet neque. Curabitur eget sem et nisl vestibulum aliquam ac ut quam. Donec in faucibus erat, in tempus ante. Etiam et neque nec metus pellentesque viverra sit amet sed dolor. Duis quis elit arcu. Fusce dapibus erat eu dolor consectetur mattis. Duis hendrerit quam dolor. Suspendisse at nisl nunc.\n\nSuspendisse sit amet odio nec turpis facilisis vehicula. In vitae leo eros. Maecenas lobortis sem et purus bibendum scelerisque. Vivamus dui leo, bibendum ut rutrum quis, feugiat sed est. Phasellus eleifend consequat lacus. Fusce a urna blandit, sodales massa eu, lacinia nisl. Morbi commodo laoreet sagittis. Nam sed gravida magna, quis eleifend dolor. Phasellus hendrerit quam vitae rhoncus eleifend. Cras id molestie velit. Donec at interdum mi. Donec quam sapien, scelerisque rutrum quam eget, aliquet scelerisque erat.\n\nAenean sit amet vestibulum est. Proin ac pharetra magna, a aliquet lacus. Ut ante quam, posuere quis eros sed, porta luctus orci. Nulla elementum metus et erat sagittis luctus. Nullam a malesuada sem. Donec turpis est, porttitor ut pellentesque sit amet, porttitor et enim. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nulla nec mollis libero. Sed consectetur elit augue, pulvinar interdum sapien tempus ac.\n\nDuis venenatis magna quis neque gravida, in luctus sem faucibus. Pellentesque metus felis, finibus a lectus ac, mattis volutpat nisl. Praesent tempus malesuada vehicula. Maecenas quis aliquam mi. Etiam elit nulla, efficitur at nisl eu, rutrum tincidunt erat. Duis vulputate iaculis felis, vel ultricies nibh placerat sed. Quisque quis libero in ex porta malesuada. Vestibulum non ante at eros porta fringilla sit amet sit amet diam. Morbi imperdiet vestibulum urna in tempus. Nullam non augue rutrum, elementum lectus placerat, convallis libero. Praesent maximus risus at tristique malesuada. In auctor eros ut sodales lacinia. Sed sagittis enim eros, vel interdum purus viverra in. Proin sit amet justo in justo semper finibus id in risus. Proin in lorem metus. Mauris sit amet consectetur nisi.\n\nMaecenas mattis pulvinar mollis. Cras lobortis lectus ut libero viverra, id bibendum ipsum commodo. Aenean vel purus id nibh luctus iaculis. Aliquam neque diam, sagittis vitae metus ac, luctus sagittis orci. Ut nec metus a leo blandit facilisis non quis lorem. Nam eget neque sem. Nunc ultricies pulvinar urna et ullamcorper. Nunc lobortis, mauris nec dictum sagittis, eros odio accumsan velit, eget interdum dolor neque a magna. Donec rhoncus justo at metus maximus molestie. Nunc elit enim, porta sed arcu nec, faucibus fringilla sem. Nulla pretium tincidunt lectus id congue. Sed elementum pretium ipsum, nec scelerisque libero interdum sed. Vestibulum dapibus quis eros quis accumsan. Aliquam malesuada, enim posuere sagittis aliquam, sapien lorem semper orci, ut tempor lacus odio non elit. Donec vitae ex sed dui eleifend rutrum.\n\nCras tempus consectetur imperdiet. Aliquam aliquet risus erat, a efficitur neque malesuada aliquet. Ut efficitur accumsan magna, et dapibus sem pulvinar et. Fusce tempor consectetur magna, ut aliquam quam posuere eget. Mauris ullamcorper aliquam sem eget dictum. Nulla varius ligula et quam imperdiet pretium. Aenean fringilla elit lacus, eget tempus odio hendrerit ac. Proin condimentum velit ac felis mattis, quis vulputate mi feugiat. Nullam maximus nisl id ante auctor, sit amet interdum tellus scelerisque. Fusce nibh orci, ultrices nec magna sed, mollis molestie ligula.\n\nDonec ante urna, fringilla id magna a, rhoncus sollicitudin enim. Fusce varius turpis sed euismod interdum. In est eros, placerat sed sem finibus, faucibus interdum leo. Phasellus orci lectus, consectetur ut molestie vel, faucibus et felis. In rhoncus velit purus, a pharetra ante tempor ac. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Vestibulum dui augue, lobortis et sem convallis, bibendum rutrum arcu. Quisque eleifend vel lorem ac euismod. In sed dapibus tortor. Fusce sed felis velit. Etiam semper nulla non mollis rhoncus. Vivamus elit sem, pulvinar ac molestie vel, convallis non purus. Phasellus finibus, tellus ut ultrices egestas, arcu arcu vehicula magna, auctor aliquam nulla eros sed augue.\n\nQuisque sagittis sapien in sem feugiat molestie. Duis vestibulum porttitor aliquet. Phasellus sapien magna, ornare nec tristique vitae, pellentesque nec dui. Sed vestibulum lorem eget convallis dapibus. Sed massa velit, faucibus ac massa ut, finibus finibus metus. Pellentesque ac faucibus eros. Ut maximus dui at justo hendrerit, at venenatis leo fermentum. Duis tincidunt at diam sit amet faucibus. Quisque ornare in leo a tristique. Fusce commodo faucibus nisl vitae ultrices. Nullam non lacus tempus, feugiat ante vitae, ultricies urna. Phasellus ornare hendrerit velit, a bibendum tellus rutrum sed.\n\nPraesent vestibulum urna nulla, gravida convallis enim tincidunt sit amet. Aliquam nec diam ligula. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Integer a lacus suscipit, mattis augue ut, vehicula felis. Mauris malesuada rutrum sem quis feugiat. Maecenas euismod, risus a hendrerit iaculis, nisl turpis cursus diam, eu luctus turpis leo a diam. Cras ut dui mi. Aliquam quis odio lectus. Nam lobortis faucibus mauris, at tincidunt ligula pellentesque vel. Integer a erat vel lacus maximus tincidunt. Etiam eu finibus eros, at accumsan tortor. Praesent porta iaculis tellus, fringilla hendrerit nisl pharetra sed.\n\nNullam id nibh id nunc sollicitudin mattis a vitae libero. Donec lacus sem, faucibus sed auctor in, eleifend at mauris. Praesent semper fermentum metus, ut commodo felis aliquam at. Etiam dapibus posuere nisi. In rutrum aliquet laoreet. Pellentesque et cursus magna. Sed tristique ex eu justo porta vulputate. Curabitur nec dui varius, tincidunt felis id, accumsan erat. Nullam sit amet felis nibh. Proin lobortis eros a est pulvinar scelerisque. Integer suscipit enim ac justo luctus suscipit.\n\nMauris nec neque dui. Nulla accumsan interdum congue. Ut et orci varius, euismod eros id, suscipit nulla. Aliquam erat volutpat. Nulla hendrerit ante vehicula consequat tincidunt. Ut accumsan sed purus ac semper. Nulla gravida ac nisl et tincidunt. Quisque accumsan elit diam, et imperdiet lectus posuere eu. Donec vulputate nibh at nunc dictum, id luctus libero porta.\n\nPraesent sit amet posuere orci, ut semper ligula. Aliquam vel bibendum ligula. Nam sit amet tempus arcu. Nullam dignissim leo felis, quis dignissim lectus eleifend vitae. In ac mattis lacus. Cras commodo ex id luctus porta. Pellentesque et justo dignissim, mattis sapien sed, varius nisl. Integer molestie risus semper justo iaculis dapibus. Aenean id rhoncus ante, vitae ornare nibh. Curabitur sed malesuada tellus. Nullam a euismod augue, facilisis cursus dui. Aliquam erat volutpat. Nunc ante nibh, aliquam a euismod a, porttitor a neque. Vivamus sit amet purus vitae risus tincidunt finibus vel eu elit. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Integer libero tortor, rhoncus in justo eu, varius varius neque.\n\nPellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Cras faucibus mauris at nisl tempus volutpat non nec justo. Ut commodo sagittis elementum. Integer fermentum nisl ligula, sed accumsan neque hendrerit vitae. Donec fermentum felis nec auctor tristique. Praesent pharetra convallis felis eget luctus. Quisque ultrices, neque id suscipit iaculis, tellus orci sodales eros, vel mattis urna felis eget odio. Sed commodo accumsan turpis sed mollis. Curabitur feugiat tempor erat id fringilla. Vestibulum lacinia turpis nec tellus lacinia, sit amet condimentum lacus aliquam. Aenean vulputate luctus nisl sed convallis. Interdum et malesuada fames ac ante ipsum primis in faucibus. Vestibulum ut dignissim nisi. Phasellus rhoncus lacus eu imperdiet porttitor. In sed ante elementum, mattis sem ac, sagittis nisl. Nullam pharetra ligula quis tortor hendrerit, at cursus metus tempus.\n\nInterdum et malesuada fames ac ante ipsum primis in faucibus. Donec sapien quam, maximus sed tristique vel, euismod ac nisl. Aliquam vel est in nisl pharetra tincidunt et tempor erat. Fusce at magna quis est vestibulum tristique. Aenean vitae augue sit amet magna mollis rhoncus. Nam a massa erat. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras rutrum massa nec pulvinar gravida. Nam ornare non felis sed semper. Aliquam ornare, magna sit amet vulputate facilisis, nulla sem egestas erat, in auctor ligula dolor nec magna. Donec quis neque eu mi bibendum hendrerit a vel arcu.\n\nDonec a tortor iaculis, placerat purus eget, rhoncus urna. Proin dignissim felis diam, vitae posuere dolor fermentum non. Donec suscipit diam lorem, auctor volutpat dui sagittis id. Sed pellentesque malesuada arcu, ut ornare ex rutrum a. Sed aliquam laoreet massa in auctor. Curabitur vitae aliquet arcu. Suspendisse ullamcorper convallis massa, eget porta sem ultrices vitae. Suspendisse potenti. Curabitur a dictum lectus, sit amet sodales mi. Nam aliquet enim ut tempus tincidunt. Phasellus at tincidunt dolor. Nulla ornare nibh fermentum finibus consequat. Ut nec metus vitae nisi pretium suscipit eu sit amet orci. Suspendisse volutpat nulla quis lorem pellentesque maximus. Ut eleifend augue quis eleifend porttitor. Integer pulvinar leo eu nibh convallis maximus.\n\nIn eget tempus tortor. Integer sagittis, sem nec dictum eleifend, nibh velit mattis felis, in imperdiet massa libero sit amet est. Praesent tristique pretium mauris. Morbi nec metus vel nisl sagittis rhoncus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur condimentum diam purus, ac pulvinar ex faucibus at. Etiam sit amet vehicula tortor. Suspendisse consectetur nec orci vitae molestie. Nulla facilisi. Mauris dignissim eu neque et porttitor. Quisque placerat velit ut ante iaculis, non semper lacus rhoncus. Proin ornare dolor eget libero feugiat, eget ultrices ante elementum. Maecenas ut odio vitae orci mollis consequat eget ac ipsum. Nullam congue euismod orci, non dignissim mi luctus quis. Suspendisse ultricies urna eget diam sollicitudin luctus. Sed elementum, odio vitae feugiat semper, eros eros hendrerit ante, sollicitudin elementum libero quam in diam.\n\nProin blandit auctor porttitor. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nulla non justo in nunc viverra porttitor. Praesent non rutrum orci. Aenean non porta lorem. Duis efficitur ac leo ut vehicula. Donec condimentum sodales venenatis. Cras dictum enim et ante vestibulum blandit. Nunc ac ante tortor. Curabitur id odio quis lorem laoreet consequat.\n\nNam porta, ligula at lobortis lobortis, massa quam aliquet odio, eu tincidunt arcu diam quis eros. Curabitur ligula purus, tempor a libero rhoncus, consectetur rhoncus enim. Mauris ac lacinia odio, ut tincidunt magna. Phasellus iaculis magna id porta iaculis. Cras et eros ut orci viverra tincidunt at id sem. Etiam sed eros sem. Maecenas a arcu augue. In quis lobortis velit, et placerat dui. Maecenas non blandit nibh. Integer dictum, tellus eget efficitur blandit, nulla erat sagittis lorem, in faucibus quam lorem sit amet magna.\n\nInteger luctus tempus libero, id accumsan dui tincidunt quis. Integer porta est ex, nec mattis turpis auctor in. Aliquam auctor magna ultrices nulla tincidunt, ut pretium metus tempus. Cras condimentum quam metus, quis malesuada augue ornare non. Phasellus vel arcu sit amet diam tincidunt suscipit. Nullam volutpat sit amet risus in malesuada. Donec eget tincidunt nunc, quis finibus lectus. Quisque finibus libero quis dui tempus, vitae ultricies est condimentum. In fringilla vulputate nibh, eget facilisis neque mattis quis. In vel consequat justo. Etiam dictum pulvinar mi, a dignissim velit ultrices ut. Maecenas molestie lectus non sodales lacinia.\n\nMorbi at accumsan mi. Pellentesque id erat eu urna convallis mollis. Fusce turpis libero, fringilla et diam vel, pretium pharetra leo. Pellentesque vitae ullamcorper felis. Pellentesque lectus ante, malesuada quis feugiat sit amet, sollicitudin sit amet ex. Nam quis rutrum quam, id blandit ante. Curabitur sollicitudin eros et arcu porttitor, sed lacinia libero accumsan. Quisque vitae nibh quis ipsum ornare aliquam. Mauris ut ullamcorper mauris, at interdum felis. Donec vehicula nunc a diam sodales aliquet. Duis tempor pulvinar quam a ullamcorper. Pellentesque nec dictum lectus, ut tempor diam.\n\nCras ac semper libero, vel varius arcu. Etiam libero odio, fermentum nec felis eu, semper bibendum lectus. Fusce at nibh ac purus pretium sodales. In eu scelerisque elit, id fringilla ex. Nullam ac consequat felis. In quis rutrum arcu. In a sem pellentesque orci pretium tempus eget nec felis.\n\nIn tellus lacus, eleifend sit amet rutrum nec, sollicitudin ac magna. Donec facilisis elementum pharetra. Nulla sollicitudin mauris eget quam auctor, in pretium magna pretium. Maecenas mauris ligula, consequat a leo sit amet, convallis rhoncus ante. Sed id elit aliquet quam tempor hendrerit. Integer massa orci, placerat id dui sed, egestas consectetur mi. Nulla sollicitudin justo scelerisque ipsum posuere vestibulum. Proin sed accumsan tellus, nec fringilla sem. Proin lectus turpis, mollis a sem sit amet, posuere commodo ex. Duis pulvinar a erat non suscipit. In nec metus congue, cursus sem et, dignissim purus.\n\nInteger iaculis sit amet metus id dapibus. Mauris congue nibh ut velit dignissim volutpat. Nulla venenatis massa in cursus pellentesque. Proin quis hendrerit ipsum, vel efficitur justo. Mauris eu accumsan nibh. Praesent nec efficitur lorem, ac vulputate augue. Proin vitae tortor sodales nibh efficitur sagittis in posuere neque. Integer id orci non nulla porttitor laoreet.\n\nMorbi fringilla lacus a turpis commodo, vitae fermentum erat pretium. Vestibulum risus metus, tempus quis auctor et, tincidunt porta lorem. Fusce ac iaculis nulla, eget consequat erat. Etiam mollis molestie mauris, a pulvinar lacus dapibus semper. In cursus convallis est, a pharetra leo dapibus ac. Morbi dictum nisi non felis fringilla, sit amet bibendum nibh venenatis. Nullam placerat lacus vel mattis luctus. Nulla ultrices arcu eget risus accumsan, vitae pharetra elit mattis. Vestibulum aliquam fringilla tellus, non dapibus orci pharetra sed. Nullam fermentum ultricies luctus.\n\nSed vel molestie nisl. Ut ac tincidunt augue, sit amet vulputate massa. Nulla nec porttitor mauris. Curabitur sed pharetra ipsum. Aenean vehicula justo dignissim est ullamcorper, sed auctor nibh vestibulum. Praesent nec eros a magna gravida ultrices ac eget tellus. Praesent ut dolor aliquam, bibendum tortor id, fermentum nisi. Maecenas et ultricies ex, eu sollicitudin odio. Sed vitae velit faucibus, posuere lectus nec, congue erat. Praesent iaculis elit ipsum, et congue ligula ullamcorper sed. Integer viverra dictum est, eu maximus nisl scelerisque et. Praesent at leo erat.\n\nNullam hendrerit cursus justo, eu ultricies lorem molestie in. Proin non ornare neque, ac scelerisque lacus. Nullam nisl massa, commodo quis magna eu, aliquam faucibus lacus. Etiam sollicitudin feugiat urna, ut rutrum velit lacinia ultrices. Aenean commodo eros ac justo aliquam commodo. Morbi vehicula ornare metus ut tincidunt. Suspendisse efficitur lobortis varius. Donec varius ultrices ligula in vehicula.\n\nCras vitae faucibus velit, ac tristique erat. Integer posuere libero pretium libero egestas iaculis. Curabitur congue malesuada ipsum sit amet pellentesque. Curabitur dolor mauris, suscipit nec nibh in, rutrum ornare sapien. Aliquam erat volutpat. Duis volutpat ipsum ex, vel pharetra velit eget."
      "Lyhyt viesti sellaisenaan: PatternLayout: %cm" in {
        val lorem = LOREM_IPSUM.take(LogConfiguration.logMessageMaxLength)
        logger.info(lorem)
        latestMaskedMessage should equal(lorem)
        latestMaskedMessage.length should equal(LogConfiguration.logMessageMaxLength)
      }
      "Leikkaa pitka viesti: PatternLayout: %cm" in {
        logger.info(LOREM_IPSUM)
        latestMaskedMessage should not equal(LOREM_IPSUM)
        latestMaskedMessage.length should equal(LogConfiguration.logMessageMaxLength)
      }
      "Lyhyt viesti sellaisenaan: JsonTemplateLayout: MaskedLogstashJsonEventLayoutV1.json" in {
        val lorem = LOREM_IPSUM.take(LogConfiguration.logMessageMaxLength)
        logger.info(lorem)
        latestMaskedJsonMessage should equal(lorem)
        latestMaskedJsonMessage.length should equal(LogConfiguration.logMessageMaxLength)
      }
      "Leikkaa pitka viesti: JsonTemplateLayout: MaskedLogstashJsonEventLayoutV1.json" in {
        logger.info(LOREM_IPSUM)
        latestMaskedJsonMessage should not equal(LOREM_IPSUM)
        latestMaskedJsonMessage.length should equal(LogConfiguration.logMessageMaxLength)
      }

    }
    "Virheiden käsittely" - {
      "Näyttää poikkeuksen" in {
        val error = new RuntimeException("foobar")
        logger.error(error)("Virhe")
        val exception = latestJsonException
        exception("exception_class") should equal("java.lang.RuntimeException")
        exception("exception_message") should equal("foobar")
        exception("stacktrace") should startWith("java.lang.RuntimeException: foobar\n\tat fi.oph.koski.log.LogMaskingPatternConverterSpec.$anonfun$new")
      }
    }
  }

  private def latestOriginalMessage = StubLogs.getLogs("PluginTest").last
  private def latestMaskedMessage = StubLogs.getLogs("PluginTestMasked").last
  private def latestJsonMessage = latestJsonProperty("PluginTestJSON", "message").toString
  private def latestMaskedJsonMessage = latestJsonProperty("PluginTestMaskedJSON", "message").toString
  private def latestJsonException: Map[String, String] = latestJsonProperty("PluginTestMaskedJSON", "exception").asInstanceOf[Map[String, String]]
  private def latestJsonProperty(appenderName: String, property: String) = {
    parse(StubLogs.getLogs(appenderName).last) match {
      case o: JObject => o.values(property)
      case o: Any => o.toString
    }
  }
}
