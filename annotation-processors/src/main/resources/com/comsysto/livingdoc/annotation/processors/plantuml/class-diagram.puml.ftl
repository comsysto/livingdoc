<#-- @ftlvariable name="parts" type="java.util.List<com.comsysto.livingdoc.annotation.processors.plantuml.ClassDiagramPart>" -->
<#import "types.part.ftl" as types>
@startuml
<#list getIncludeFiles() as includeFile>
!include ${includeFile}
</#list>

<#list parts as part>
    <@types.renderType part/>
</#list>

<#list getInheritanceAssociations() as association>
    <@types.renderAssociation association/>
</#list>
@enduml
