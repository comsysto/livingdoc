<#macro renderType part>
<#-- @ftlvariable name="part" type="com.comsysto.livingdoc.annotation.processors.plantuml.model.ClassDiagramPart" -->
    <#if part.isInterface()>
        <#local typeDeclaration="interface">
    <#elseif part.isAbstract()>
        <#local typeDeclaration="abstract class">
    <#elseif part.isEnum()>
        <#local typeDeclaration="enum">
    <#else>
        <#local typeDeclaration="class">
    </#if>
${typeDeclaration} ${part.name}
    <#list part.notes as note>
note ${note.position()?lower_case} of ${part.name}
${note.body()}
end note

    </#list>
</#macro>

<#macro renderAssociation association>
<#-- @ftlvariable name="association" type="com.comsysto.livingdoc.annotation.processors.plantuml.model.InheritenceAssociationPart" -->
    <#if association.relation="EXTENDS">
        <#local relationOperator="<--">
    <#elseif association.relation="IMPLEMENTS">
        <#local relationOperator="<..">
    <#else>
        <#local relationOperator="-">
    </#if>
${association.left.simpleName} ${relationOperator} ${association.right.simpleName}
</#macro>