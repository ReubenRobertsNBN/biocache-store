package au.org.ala.biocache

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Assertions.expect
import au.org.ala.biocache.vocab._

/**
 * Test for vocabulary mapping and lookups.
 */
@RunWith(classOf[JUnitRunner])
class VocabTest extends ConfigFunSuite {

  test("State province mapping") {
    expectResult("Australia") { StateProvinceToCountry.map.getOrElse("New South Wales", "")}
  }

  test("Basis of record matching"){
    expectResult("PreservedSpecimen"){ BasisOfRecord.matchTerm("PRESERVED_SPECIMEN").get.canonical }
    expectResult("PreservedSpecimen"){ BasisOfRecord.matchTerm("speci").get.canonical }
    expectResult("PreservedSpecimen"){ BasisOfRecord.matchTerm("S").get.canonical }
  }

  test("Type status matching"){
    expectResult("isotype"){ TypeStatus.matchTerm("isotype").get.canonical}
  }

  test("Basis of record matching with junk characters"){
	  expectResult("syntype"){ TypeStatus.matchTerm("Syntype ?").get.canonical}
  }

  test("Expect non marine to match terrestrial"){
	  expectResult(true){HabitatMap.isCompatible("non-marine", "terrestrial").get}
  }

  test("Expect  marine to not match terrestrial"){
	  expectResult(false){HabitatMap.isCompatible("marine", "terrestrial").get}
  }
  
  test("Expect marine and non-marine to match all"){
    expectResult(true){HabitatMap.isCompatible("marine","marine and non-marine").get}
    expectResult(true){HabitatMap.isCompatible("non-marine","marine and non-marine").get}
    expectResult(true){HabitatMap.isCompatible("terrestrial","marine and non-marine").get}
    expectResult(true){HabitatMap.isCompatible("limnetic","marine and non-marine").get}
  }

  test("Expect coordinates for QLD centre to match"){
    expectResult(false){StateProvinceCentrePoints.coordinatesMatchCentre("QLD","12","12")}
    expectResult(true){StateProvinceCentrePoints.coordinatesMatchCentre("QLD","-20.9175738","142.7027956")}
    expectResult(true){StateProvinceCentrePoints.coordinatesMatchCentre("QLD","-20.917573","142.702795")}
    expectResult(true){StateProvinceCentrePoints.coordinatesMatchCentre("QLD","-20.917","142.702")}
  }
  
  test("Coordinates unknown state"){
    StateProvinceCentrePoints.coordinatesMatchCentre("Unknown1","-54.50285462","158.9173835")
  }

  test("Expect coordinates for Australia centre to match"){
    expectResult(false){CountryCentrePoints.coordinatesMatchCentre("Australia","12","12")}
    expectResult(false){CountryCentrePoints.coordinatesMatchCentre("   ","12","12")}
    expectResult(true){CountryCentrePoints.coordinatesMatchCentre("Australia","-29.5328037","145.491477")}
    expectResult(true){CountryCentrePoints.coordinatesMatchCentre("Australia","-29.53280","145.4914")}
    expectResult(true){CountryCentrePoints.coordinatesMatchCentre("Australia","-29.532","145.491")}
  }

//NC TODO This test needs to pass in order to support the DWC standard for the TypeStatus
//  test("Holotype with extra info"){
//    println(TypeStatus.matchTerm("Holotype: Scrobs pyramidatus Hedley, 1903 : Rissoidae : : Gastropoda : Mollusca"))
//    expectResult(false) {TypeStatus.matchTerm("Holotype: Scrobs pyramidatus Hedley, 1903 : Rissoidae : : Gastropoda : Mollusca").isEmpty}
//  }

  test("Paratype - case insensitive for types"){
    expectResult(false){ TypeStatus.matchTerm("Paratype").isEmpty}
  }

  test("Paratype - not plurals for types"){
    expectResult(false){ TypeStatus.matchTerm("Paratype").isEmpty}
    expectResult("paratype"){TypeStatus.matchTerm("Paratypes").get.canonical}
  }
  
  test("Observations - plurals for BOR"){
    expectResult(false){ BasisOfRecord.matchTerm("Observation").isEmpty}
  }

  test("Test Australia hemispheres"){
    expectResult(Set('S','E','W')){CountryCentrePoints.getHemispheres("Australia").get}
  }

  test("Match Mongolia"){
    expectResult("mongolia"){Countries.matchTerm("Mongolia").get.canonical.toLowerCase}
  }

  test("Match UK"){
    expectResult("united kingdom"){Countries.matchTerm("United Kingdom").get.canonical.toLowerCase}
  }

  test("Test UK hemispheres"){
    expectResult(Set('E','W', 'N')){CountryCentrePoints.getHemispheres("United Kingdom").get}
  }

  test("S for specimen"){
    expectResult("PreservedSpecimen"){BasisOfRecord.matchTerm("S").get.canonical}
  }

  test("DigitisedTrack"){
    expectResult("Sound"){BasisOfRecord.matchTerm("DigitisedTrack").get.canonical}
  }

  test("Our dog food"){
    val downloadFieldNames = List("Catalog Number","Match Taxon Concept GUID","Scientific Name","Vernacular Name",
      "Matched Scientific Name","Taxon Rank - matched","Vernacular Name - matched","Kingdom - matched",
      "Phylum - matched","Class - matched","Order - matched","Family - matched","Genus - matched","Species - matched",
      "Subspecies - matched","Institution Code","Collection Code","Latitude - processed","Longitude - processed",
      "Coordinate Precision","Country - parsed","IBRA Region - parsed","IMCRA Region - parsed","State - parsed",
      "Local Government Area - parsed","Minimum Elevation In Metres","Maximum Elevation In Metres",
      "Minimum Depth In Meters","Maximum Depth In Meters","Year - parsed","Month - parsed","Day - parsed",
      "Event Date - parsed","Event Time - parsed","Basis Of Record","Sex","Preparations")

    downloadFieldNames.foreach(name => {
      expectResult(false) { DwC.matchTerm(name).isEmpty }
    })
  }
  
  test("establishmentMeans"){
    expectResult("formerly cultivated (extinct)"){ EstablishmentMeans.matchTerm("formerly cultivated (extinct)").get.canonical }
  }

  test("Mime Types") {
    expectResult("image/jpeg"){MimeType.matchTerm("image/jpeg").get.canonical}
    expectResult("image/jpeg"){MimeType.matchTerm("jpeg").get.canonical}
    expectResult("image/jpeg"){MimeType.matchTerm("JPEG").get.canonical}
    expectResult("image/png"){MimeType.matchTerm("png").get.canonical}
    expectResult("image/gif"){MimeType.matchTerm("gif").get.canonical}
    expectResult("audio/mp3"){MimeType.matchTerm("mp3").get.canonical}
    expectResult("video/mp4"){MimeType.matchTerm("mp4").get.canonical}
  }

  test("licences no cache") {
    expectResult("CC-BY 3.0 (Au)"){License.matchRegex("CC-BY 3.0 Au").get.canonical}
    expectResult("CC-BY 3.0 (Au)"){License.matchRegex("CreativeCommons By 3.0 Au").get.canonical}
    expectResult("CC-BY 4.0 (Au)"){License.matchRegex("CC-BY Au").get.canonical}
    expectResult("CC-BY 3.0 (Int)"){License.matchRegex("CC-BY 3.0").get.canonical}
    expectResult("CC-BY 4.0 (Int)"){License.matchRegex("CC-BY").get.canonical}
    expectResult("CC-BY-SA 2.5 (Au)"){License.matchRegex("CC-BY/SA 2.5 (Au)").get.canonical}
    expectResult("CC-BY-SA 4.0 (Au)"){License.matchRegex("CC-BY/SA (Au)").get.canonical}
    expectResult("CC-BY-SA 3.0 (Int)"){License.matchRegex("CC:BY:SA 3.0").get.canonical}
    expectResult("CC-BY-SA 4.0 (Int)"){License.matchRegex("CC.BY.SA").get.canonical}
    expectResult("CC-BY-NC 4.0 (Int)"){License.matchRegex("CC-BY-NC").get.canonical}
    expectResult("CC-BY-ND 4.0 (Int)"){License.matchRegex("CC-BY-ND").get.canonical}
    expectResult("CC-BY-NC-SA 3.0 (Au)"){License.matchRegex("CC-BY-NC-SA-3.0-AU").get.canonical}
    expectResult("CC-BY-NC-SA 3.0 (Int)"){License.matchRegex("CC-BY-NC-SA 3.0").get.canonical}
    expectResult("CC-BY-NC-SA 4.0 (Int)"){License.matchRegex("CC-BY-NC-SA").get.canonical}
    expectResult("Custom"){License.matchRegex("Rotate widdershins").get.canonical}
  }

  test("licences with cache") {
    expectResult("CC-BY 3.0 (Au)"){License.matchRegexCached("CC-BY 3.0 Au").get.canonical}
    expectResult("CC-BY 3.0 (Au)"){License.matchRegexCached("CC-BY 3.0 Au").get.canonical}
    expectResult("CC-BY 4.0 (Int)"){License.matchRegexCached("CC-BY").get.canonical}
    expectResult("Custom"){License.matchRegexCached("Rotate widdershins").get.canonical}
  }

}