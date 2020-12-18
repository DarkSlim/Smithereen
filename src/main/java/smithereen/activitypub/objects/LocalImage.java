package smithereen.activitypub.objects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;

import smithereen.Config;
import smithereen.activitypub.ContextCollector;
import smithereen.activitypub.ParserContext;
import smithereen.data.SizedImage;
import smithereen.storage.ImgProxy;

public class LocalImage extends Image implements SizedImage{
	public String path;
	public Dimensions size=Dimensions.UNKNOWN;

	@Override
	protected ActivityPubObject parseActivityPubObject(JSONObject obj, ParserContext parserContext) throws Exception{
		super.parseActivityPubObject(obj, parserContext);
		localID=obj.getString("_lid");
		JSONArray s=obj.getJSONArray("_sz");
		path=obj.optString("_p", "post_media");
		width=s.getInt(0);
		height=s.getInt(1);
		size=new Dimensions(width, height);
		return this;
	}

	@Override
	public JSONObject asActivityPubObject(JSONObject obj, ContextCollector contextCollector){
		obj=super.asActivityPubObject(obj, contextCollector);
		ImgProxy.UrlBuilder builder=new ImgProxy.UrlBuilder("local://"+Config.uploadURLPath+"/"+path+"/"+localID+".webp")
				.format(SizedImage.Format.JPEG);
		int croppedWidth=width, croppedHeight=height;
		if(cropRegion!=null){
			int x=Math.round(cropRegion[0]*width);
			int y=Math.round(cropRegion[1]*height);
			builder.crop(x, y, croppedWidth=Math.round(cropRegion[2]*width-x), croppedHeight=Math.round(cropRegion[3]*height-y));
		}
		obj.put("url", builder.build().toString());
		obj.put("width", croppedWidth);
		obj.put("height", croppedHeight);
		if(cropRegion!=null){
			Image im=new Image();
			im.width=width;
			im.height=height;
			im.url=new ImgProxy.UrlBuilder("local://"+Config.uploadURLPath+"/"+path+"/"+localID+".webp")
					.format(SizedImage.Format.JPEG)
					.build();
			obj.put("image", im.asActivityPubObject(null, contextCollector));
		}
		return obj;
	}

	@Override
	public URI getUriForSizeAndFormat(Type size, Format format){
		ImgProxy.UrlBuilder builder=new ImgProxy.UrlBuilder("local://"+Config.uploadURLPath+"/"+path+"/"+localID+".webp")
				.format(format)
				.resize(size.getResizingType(), size.getMaxWidth(), size.getMaxHeight(), false, false);
		if(cropRegion!=null && size.getResizingType()==ImgProxy.ResizingType.FILL){
			int x=Math.round(cropRegion[0]*width);
			int y=Math.round(cropRegion[1]*height);
			builder.crop(x, y, Math.round(cropRegion[2]*width-x), Math.round(cropRegion[3]*height-y));
		}
		return builder.build();
	}

	@Override
	public Dimensions getOriginalDimensions(){
		return size;
	}
}
