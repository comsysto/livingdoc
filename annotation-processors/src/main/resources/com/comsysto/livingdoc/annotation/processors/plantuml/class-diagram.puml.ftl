<#-- @ftlvariable name="parts" type="java.util.List<com.comsysto.livingdoc.annotation.processors.plantuml.ClassDiagramPart>" -->
<#import "types.part.ftl" as types>
@startuml
<#list getIncludeFiles() as includeFile>
!include ${includeFile}
</#list>

<#list parts as part>
    <@types.type part/>
</#list>

<#list getInheritanceAssociations() as association>
    <@types.inheritanceAssociation association/>
</#list>
@enduml
