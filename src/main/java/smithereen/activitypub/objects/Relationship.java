package smithereen.activitypub.objects;

import org.json.JSONObject;

import java.net.URI;

import smithereen.activitypub.ContextCollector;
import smithereen.activitypub.ParserContext;

public class Relationship extends ActivityPubObject{

	public static final URI FRIEND_OF=URI.create("http://purl.org/vocab/relationship/friendOf");

	public LinkOrObject subject;
	public LinkOrObject object;
	public URI relationship;

	@Override
	public String getType(){
		return "Relationship";
	}

	@Override
	public JSONObject asActivityPubObject(JSONObject obj, ContextCollector contextCollector){
		obj=super.asActivityPubObject(obj, contextCollector);
		obj.put("relationship", relationship.toString());
		obj.put("object", object.serialize(contextCollector));
		obj.put("subject", subject.serialize(contextCollector));
		return obj;
	}

	@Override
	protected ActivityPubObject parseActivityPubObject(JSONObject obj, ParserContext parserContext) throws Exception{
		super.parseActivityPubObject(obj, parserContext);
		relationship=tryParseURL(obj.getString("relationship"));
		object=tryParseLinkOrObject(obj.get("object"), parserContext);
		subject=tryParseLinkOrObject(obj.get("subject"), parserContext);
		return this;
	}
}
