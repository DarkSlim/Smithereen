<?php

$mf=json_decode(file_get_contents("https://w3c.github.io/json-ld-api/tests/expand-manifest.jsonld"));
$j=[
"package smithereen.jsonld;",
"",
"import org.junit.jupiter.api.DisplayName;",
"import org.junit.jupiter.api.Test;",
"",
"import java.io.*;",
"import java.nio.charset.StandardCharsets;",
"import java.net.URI;",
"",
"import org.json.*;",
"",
"import static org.junit.jupiter.api.Assertions.*;",
"",
"class ExpandTests{",
"",
"	private Object readResource(String name){",
"		InputStream in=getClass().getResourceAsStream(name);",
"		try{",
"			byte[] buf=new byte[in.available()];",
"			in.read(buf);",
"			in.close();",
"			String s=new String(buf, StandardCharsets.UTF_8);",
"			if(s.charAt(0)=='[')",
"				return new JSONArray(s);",
"			return new JSONObject(s);",
"		}catch(IOException ignore){}",
"		return null;",
"	}",
""
];

foreach($mf->sequence as $test){
	if(isset($test->option) && isset($test->option->specVersion) && $test->option->specVersion!="json-ld-1.0"){
		echo "Skipping test {$test->name} because of incompatible spec version\n";
		continue;
	}
	if($test->{"@id"}=="#te051") continue; // input file is 404
	if($test->{"@id"}=="#t0077") continue; // external contexts aren't going to happen in real life
	$j[]="\t/***\n\t* {$test->purpose}\n\t*/";
	$j[]="\t@Test";
	$j[]="\t@DisplayName(\"".str_replace('"', '\"', $test->name)."\")";
	$j[]="\tvoid ".substr($test->{"@id"}, 1)."(){";
	$type=$test->{"@type"}[0];
	$inputURL="https://w3c.github.io/json-ld-api/tests/".$test->input;
	if(!file_exists("src/test/resources/".$test->input)){
		file_put_contents("src/test/resources/".$test->input, file_get_contents($inputURL));
	}
	if(isset($test->expect) && !file_exists("src/test/resources/".$test->expect)){
		file_put_contents("src/test/resources/".$test->expect, file_get_contents("https://w3c.github.io/json-ld-api/tests/".$test->expect));
	}
	if(isset($test->option) && isset($test->option->base))
		$inputURL=$test->option->base;
	if($type=="jld:PositiveEvaluationTest"){
		$j[]="\t\tObject input=readResource(\"/{$test->input}\");";
		$j[]="\t\tObject expect=readResource(\"/{$test->expect}\");";
		$j[]="\t\tJSONArray expanded=JLDDocument.expandToArray(input, URI.create(\"$inputURL\"));";
		$j[]="\t\tJLDUtilities.assertEqualJLD(expect, expanded);";
	}else if($type=="jld:NegativeEvaluationTest"){
		$j[]="\t\tassertThrows(JLDException.class, ()->{";
		$j[]="\t\t\tObject input=readResource(\"/{$test->input}\");";
		$j[]="\t\t\tJLDDocument.expandToArray(input, URI.create(\"$inputURL\"));";
		$j[]="\t\t}, \"{$test->expectErrorCode}\");";
	}else{
		die("Unknown test type $type\n");
	}
	$j[]="\t}";
	$j[]="";
}

$j[]="}";

file_put_contents("src/test/java/smithereen/jsonld/ExpandTests.java", implode("\n", $j));