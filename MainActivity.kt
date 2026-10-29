package fr.macdtheque

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

data class Cd(val id:Long=0,val title:String="",val artist:String="",val year:String="",val format:String="",val genre:String="",val ean:String="",val ref:String="",val cover:String="")
class Store(ctx:Context):SQLiteOpenHelper(ctx,"collection.db",null,1){
 override fun onCreate(db:SQLiteDatabase){db.execSQL("CREATE TABLE cd(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,artist TEXT,year TEXT,format TEXT,genre TEXT,ean TEXT,ref TEXT,cover TEXT)")}
 override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){}
 fun add(cd:Cd){val v=ContentValues().apply{put("title",cd.title);put("artist",cd.artist);put("year",cd.year);put("format",cd.format);put("genre",cd.genre);put("ean",cd.ean);put("ref",cd.ref);put("cover",cd.cover)};writableDatabase.insert("cd",null,v)}
 fun remove(id:Long){writableDatabase.delete("cd","id=?",arrayOf(id.toString()))}
 fun updateCover(id:Long,url:String){val v=ContentValues().apply{put("cover",url)};writableDatabase.update("cd",v,"id=?",arrayOf(id.toString()))}
 fun all():List<Cd>{val list=mutableListOf<Cd>();readableDatabase.rawQuery("SELECT id,title,artist,year,format,genre,ean,ref,cover FROM cd ORDER BY artist COLLATE NOCASE,title COLLATE NOCASE",null).use{c->while(c.moveToNext())list.add(Cd(c.getLong(0),c.getString(1)?:"",c.getString(2)?:"",c.getString(3)?:"",c.getString(4)?:"",c.getString(5)?:"",c.getString(6)?:"",c.getString(7)?:"",c.getString(8)?:""))};return list}
 fun initialize(ctx:Context){val p=ctx.getSharedPreferences("init",0);if(p.getBoolean("done",false))return;val arr=JSONArray(ctx.assets.open("cds.json").bufferedReader().use{it.readText()});writableDatabase.beginTransaction();try{for(i in 0 until arr.length()){val j=arr.getJSONObject(i);add(Cd(title=j.optString("TITRE"),artist=j.optString("ARTISTE"),year=j.optString("ANNEE"),format=j.optString("FORMAT"),genre=j.optString("GENRE"),ean=j.optString("EAN"),ref=j.optString("REF")))};writableDatabase.setTransactionSuccessful();p.edit().putBoolean("done",true).apply()}finally{writableDatabase.endTransaction()}}
}
private val Violet=Color(0xFF7155CC)
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);val store=Store(this);store.initialize(this);setContent { MaterialTheme(colorScheme=lightColorScheme(primary=Violet,secondary=Color(0xFFFF8C55),background=Color(0xFFF9F7FF))) { Screen(this,store) } }}
}
private fun findCover(ean:String, callback:(String)->Unit){if(ean.isBlank())return;thread {try{
 val query=URLEncoder.encode("barcode:$ean","UTF-8")
 val connection=URL("https://musicbrainz.org/ws/2/release/?query=$query&fmt=json&limit=1").openConnection() as HttpURLConnection
 connection.setRequestProperty("User-Agent","MaCDtheque/1.0 (personal Android collection)");connection.connectTimeout=9000;connection.readTimeout=9000
 val obj=JSONObject(connection.inputStream.bufferedReader().use{it.readText()});val releases=obj.optJSONArray("releases")
 if(releases!=null && releases.length()>0){val id=releases.getJSONObject(0).getString("id");callback("https://coverartarchive.org/release/$id/front-250")}
 }catch(_:Exception){} }.start()}
@Composable fun Screen(activity:Activity,store:Store){
 var cds by remember{mutableStateOf(store.all())};var search by remember{mutableStateOf("")};var editing by remember{mutableStateOf(false)};var title by remember{mutableStateOf("")};var artist by remember{mutableStateOf("")};var ean by remember{mutableStateOf("")};var year by remember{mutableStateOf("")};var selected by remember{mutableStateOf<Cd?>(null)};var confirmDelete by remember{mutableStateOf(false)}
 fun scan(onResult:(String)->Unit){GmsBarcodeScanning.getClient(activity).startScan().addOnSuccessListener{b->b.rawValue?.let(onResult)}}
 val shown=remember(cds,search){cds.filter{search.isBlank()||listOf(it.title,it.artist,it.genre,it.ean,it.ref).any{v->v.contains(search,true)}}}
 Column(Modifier.fillMaxSize().background(Color(0xFFF9F7FF)).padding(16.dp)){
 Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Album,null,tint=Violet,modifier=Modifier.size(36.dp));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("Ma CDthèque",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("${cds.size} CD dans ma collection",color=Violet)};IconButton(onClick={title="";artist="";ean="";year="";editing=true}){Icon(Icons.Default.AddCircle,"Ajouter",tint=Violet,modifier=Modifier.size(32.dp))}}
 Spacer(Modifier.height(14.dp))
 OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("Titre, artiste, genre, EAN…")},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={IconButton(onClick={scan{search=it}}){Icon(Icons.Default.QrCodeScanner,"Scanner pour chercher")}})
 Spacer(Modifier.height(10.dp))
 Row(verticalAlignment=Alignment.CenterVertically){Text("${shown.size} résultat(s)",modifier=Modifier.weight(1f),color=Color.Gray);TextButton(onClick={scan{ean=it;title="";artist="";year="";editing=true}}){Icon(Icons.Default.QrCodeScanner,null);Text(" Scanner pour ajouter")}}
 LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp)) {items(shown,key={it.id}){cd->Card(onClick={selected=cd},colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){if(cd.cover.isNotBlank())AsyncImage(model=cd.cover,contentDescription="Pochette",modifier=Modifier.size(64.dp)) else Box(Modifier.size(64.dp).background(Color(0xFFE9DFFF),RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Icon(Icons.Default.MusicNote,null,tint=Violet)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(cd.title.ifBlank{"Sans titre"},fontWeight=FontWeight.Bold);Text(cd.artist,color=Color.DarkGray);Text(listOf(cd.year,cd.format,cd.genre).filter{it.isNotBlank()}.joinToString(" · "),style=MaterialTheme.typography.bodySmall,color=Violet)}}}}}
 }
 }
 if(editing)AlertDialog(onDismissRequest={editing=false},title={Text("Ajouter un CD")},text={Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
 OutlinedTextField(title,{title=it},label={Text("Titre *")});OutlinedTextField(artist,{artist=it},label={Text("Artiste")});OutlinedTextField(year,{year=it},label={Text("Année")});OutlinedTextField(ean,{ean=it},label={Text("Code-barres EAN")},trailingIcon={IconButton(onClick={scan{ean=it}}){Icon(Icons.Default.QrCodeScanner,null)}})
 }},confirmButton={TextButton(enabled=title.isNotBlank(),onClick={val code=ean.trim();store.add(Cd(title=title.trim(),artist=artist.trim(),year=year.trim(),ean=code));cds=store.all();editing=false;val newCd=cds.lastOrNull{it.ean==code&&it.title==title.trim()};if(newCd!=null)findCover(code){url->activity.runOnUiThread{store.updateCover(newCd.id,url);cds=store.all()}}}){Text("Enregistrer")}},dismissButton={TextButton(onClick={editing=false}){Text("Annuler")}})
 selected?.let{cd->AlertDialog(onDismissRequest={selected=null},title={Text(cd.title)},text={Column{Text(cd.artist,fontWeight=FontWeight.Bold);Text("${cd.year} · ${cd.format} · ${cd.genre}");Text("EAN : ${cd.ean.ifBlank{"Non renseigné"}}");Text("Référence : ${cd.ref}");Spacer(Modifier.height(10.dp));if(cd.cover.isNotBlank())AsyncImage(model=cd.cover,contentDescription="Pochette",modifier=Modifier.fillMaxWidth().height(180.dp));TextButton(onClick={findCover(cd.ean){url->activity.runOnUiThread{store.updateCover(cd.id,url);cds=store.all();selected=cd.copy(cover=url)}}}){Text("Rechercher la pochette")}}},confirmButton={TextButton(onClick={selected=null}){Text("Fermer")}},dismissButton={TextButton(onClick={confirmDelete=true}){Text("Supprimer",color=Color(0xFFD44343))}})}
 if(confirmDelete)AlertDialog(onDismissRequest={confirmDelete=false},title={Text("Supprimer ce CD ?")},text={Text("Cette suppression est définitive.")},confirmButton={TextButton(onClick={selected?.let{store.remove(it.id)};cds=store.all();selected=null;confirmDelete=false}){Text("Supprimer")}},dismissButton={TextButton(onClick={confirmDelete=false}){Text("Annuler")}})
}
