<#-- @ftlvariable name="diagramId" type="String" -->

<#macro renderRelations s0tModel>
<#-- @ftlvariable name="s0tModel" type="com.comsysto.livingdoc.s0t.model.S0tModel" -->
    <#list s0tModel.types?values as type>

        <#compress>
            <#list type.realizations as realization>
                ${realization.left.name.simpleName} <|.. ${realization.right.name.simpleName}
            </#list>
            <#if type.inheritance??>
                ${type.inheritance.left.name.simpleName} <|-- ${type.inheritance.right.name.simpleName}
            </#if>
            <#list type.associations as association>
                ${association.left.name.simpleName}<@renderCardinality association.sourceCardinality/><@renderArrow association/><@renderCardinality association.targetCardinality/>${association.right.name.simpleName}
            </#list>
            <#list type.dependencies as dependency>
                ${dependency.left.name.simpleName} ..> ${dependency.right.name.simpleName}<#if dependency.description?has_content>: ${dependency.description}</#if>
            </#list>
        </#compress>

    </#list>
</#macro>

<#macro renderCardinality c><#if c?length != 0> "${c}" <#else> </#if></#macro>

<#macro renderArrow association><#rt>
<#-- @ftlvariable name="association" type="com.comsysto.livingdoc.s0t.model.relations.Association" --><#lt><#rt>
    <#if association.type.name() == "STANDARD"><#lt><#rt>--><#lt><#rt>
    <#elseif association.type.name() == "COMPOSITION">*--><#lt><#rt>
    <#elseif association.type.name() == "AGGREGATION">o--><#lt><#rt>
    </#if><#lt><#rt>
<#lt></#macro>
