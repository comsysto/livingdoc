<#ftl attributes={"s0t:is-template":true}/>
<#global diagramId="airport"/>
<#-- @ftlvariable name="s0tModel" type="com.comsysto.livingdoc.s0t.model.S0tModel" -->
@startuml
!include ./format.iuml
hide empty members
title Airport class diagram

<@S0tTypes.renderTypes s0tModel/>

<@S0tRelations.renderRelations s0tModel/>

@enduml
