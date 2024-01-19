package lk.avn.irenttechs;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.StorageMetadata;
import com.squareup.picasso.Picasso;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.FirebaseStorage;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import lk.avn.irenttechs.custom.CustomLoading;
import lk.avn.irenttechs.custom.CustomTransition;
import lk.avn.irenttechs.custom.Logout;
import lk.avn.irenttechs.custom.RetrofitClient;
import lk.avn.irenttechs.dto.ProfileUpdateDTO;
import lk.avn.irenttechs.model.UserProfile;
import lk.avn.irenttechs.service.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getName();
    private static String errorName;
    private static String successName;
    private static String successtitle;

    private EditText nameTextView;
    private TextView emailTextView;
    private EditText mobileTextView;
    ListenerRegistration listenerRegistration;
    private FirebaseFirestore fireStore;
    private FirebaseStorage storage;
    private Uri imagePath;
    private ShapeableImageView imageView;
    private AutoCompleteTextView address_city;
    private AutoCompleteTextView address_district;
    private AutoCompleteTextView address_province;
    private EditText address_line1;
    private EditText address_line2;
    private EditText address_postal_code;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private View fragmentView;
    CustomLoading customLoading;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences login_preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

        if (login_preferences.getString("ID", null) == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return null;
        } else {
            return inflater.inflate(R.layout.fragment_profile, container, false);
        }
    }

    LinearLayout otherDetails1;
    LinearLayout layout1;
    LinearLayout otherDetails2;
    LinearLayout layout2;

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        if (getActivity() instanceof HomeActivity) {
            HomeActivity ha = (HomeActivity) getActivity();
            TextView t_name = ha.findViewById(R.id.toolbar_name);
            t_name.setText("Profile");
            MaterialToolbar toolbar = ha.findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(R.drawable.menu);
            ImageButton backbtn = ha.findViewById(R.id.back_btn);
            backbtn.setVisibility(View.GONE);

        }

        SharedPreferences login_preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

        if (login_preferences.getString("ID", null) == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);

        } else {


            fragmentView = fragment;

            customLoading = new CustomLoading(getActivity());


            otherDetails1 = fragment.findViewById(R.id.visiblelayout);
            layout1 = fragment.findViewById(R.id.layout);
            layout1.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            imageView = fragment.findViewById(R.id.profile_img);

            emailTextView = fragment.findViewById(R.id.profile_email_input);


            fireStore = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();

            address_city = fragment.findViewById(R.id.address_city);
            address_district = fragment.findViewById(R.id.address_district);
            address_province = fragment.findViewById(R.id.address_province);
            address_line1 = fragment.findViewById(R.id.address_line1);
            address_line2 = fragment.findViewById(R.id.address_line2);
            address_postal_code = fragment.findViewById(R.id.address_postal_code);


            SharedPreferences preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);
            String id = preferences.getString("ID", null);
            String name = preferences.getString("NAME", null);
            String email = preferences.getString("EMAIL", null);

            nameTextView = fragment.findViewById(R.id.profile_name_input);
            mobileTextView = fragment.findViewById(R.id.profile_mobile_input);

            nameTextView.setText(name);
            emailTextView.setText(email);

            loadAddress();

            try {

                StorageReference imageRef = storage.getReference("user_images/" + email);

                imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get()
                                        .load(uri)
                                        .resize(200, 200)
                                        .centerCrop()
                                        .into(imageView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "Error Image");
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "No Image");
                    }
                });


            } catch (Exception e) {
                Log.i(TAG, e.getMessage());

            }


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    final String[] city = new String[]{"Akkaraipattu", "Ambagahawatta", "Ampara", "Bakmitiyawa", "Deegawapiya", "Devalahinda", "Digamadulla Weeragoda", "Dorakumbura", "Gonagolla", "Hulannuge", "Kalmunai", "Kannakipuram", "Karativu", "Kekirihena", "Koknahara", "Kolamanthalawa", "Komari", "Lahugala", "Irakkamam", "Mahaoya", "Marathamune", "Namaloya", "Navithanveli", "Nintavur", "Oluvil", "Padiyatalawa", "Pahalalanda", "Panama", "Pannalagama", "Paragahakele", "Periyaneelavanai", "Polwaga Janapadaya", "Pottuvil", "Sainthamaruthu", "Samanthurai", "Serankada", "Tempitiya", "Thambiluvil", "Tirukovil", "Uhana", "Wadinagala", "Wanagamuwa", "Angamuwa", "Anuradhapura", "Awukana", "Bogahawewa", "Dematawewa", "Dimbulagala", "Dutuwewa", "Elayapattuwa", "Ellewewa", "Eppawala", "Etawatunuwewa", "Etaweeragollewa", "Galapitagala", "Galenbindunuwewa", "Galkadawala", "Galkiriyagama", "Galkulama", "Galnewa", "Gambirigaswewa", "Ganewalpola", "Gemunupura", "Getalawa", "Gnanikulama", "Gonahaddenawa", "Habarana", "Halmillawa Dambulla", "Halmillawetiya", "Hidogama", "Horawpatana", "Horiwila", "Hurigaswewa", "Hurulunikawewa", "Ihala Puliyankulama", "Kagama", "Kahatagasdigiliya", "Kahatagollewa", "Kalakarambewa", "Kalaoya", "Kalawedi Ulpotha", "Kallanchiya", "Kalpitiya", "Kalukele Badanagala", "Kapugallawa", "Karagahawewa", "Kashyapapura", "Kebithigollewa", "Kekirawa", "Kendewa", "Kiralogama", "Kirigalwewa", "Kirimundalama", "Kitulhitiyawa", "Kurundankulama", "Labunoruwa", "Ihalagama", "Ipologama", "Madatugama", "Maha Elagamuwa", "Mahabulankulama", "Mahailluppallama", "Mahakanadarawa", "Mahapothana", "Mahasenpura", "Mahawilachchiya", "Mailagaswewa", "Malwanagama", "Maneruwa", "Maradankadawala", "Maradankalla", "Medawachchiya", "Megodawewa", "Mihintale", "Morakewa", "Mulkiriyawa", "Muriyakadawala", "Colombo 15", "Nachchaduwa", "Namalpura", "Negampaha", "Nochchiyagama", "Nuwaragala", "Padavi Maithripura", "Padavi Parakramapura", "Padavi Sripura", "Padavi Sritissapura", "Padaviya", "Padikaramaduwa", "Pahala Halmillewa", "Pahala Maragahawe", "Pahalagama", "Palugaswewa", "Pandukabayapura", "Pandulagama", "Parakumpura", "Parangiyawadiya", "Parasangahawewa", "Pelatiyawa", "Pemaduwa", "Perimiyankulama", "Pihimbiyagolewa", "Pubbogama", "Punewa", "Rajanganaya", "Rambewa", "Rampathwila", "Rathmalgahawewa", "Saliyapura", "Seeppukulama", "Senapura", "Sivalakulama", "Siyambalewa", "Sravasthipura", "Talawa", "Tambuttegama", "Tammennawa", "Tantirimale", "Telhiriyawa", "Tirappane", "Tittagonewa", "Udunuwara Colony", "Upuldeniya", "Uttimaduwa", "Vellamanal", "Viharapalugama", "Wahalkada", "Wahamalgollewa", "Walagambahuwa", "Walahaviddawewa", "Welimuwapotana", "Welioya Project", "Akkarasiyaya", "Aluketiyawa", "Aluttaramma", "Ambadandegama", "Ambagasdowa", "Arawa", "Arawakumbura", "Arawatta", "Atakiriya", "Badulla", "Baduluoya", "Ballaketuwa", "Bambarapana", "Bandarawela", "Beramada", "Bibilegama", "Boragas", "Boralanda", "Bowela", "Central Camp", "Damanewela", "Dambana", "Dehiattakandiya", "Demodara", "Diganatenna", "Dikkapitiya", "Dimbulana", "Divulapelessa", "Diyatalawa", "Dulgolla", "Ekiriyankumbura", "Ella", "Ettampitiya", "Galauda", "Galporuyaya", "Gawarawela", "Girandurukotte", "Godunna", "Gurutalawa", "Haldummulla", "Hali Ela", "Hangunnawa", "Haputale", "Hebarawa", "Heeloya", "Helahalpe", "Helapupula", "Hopton", "Idalgashinna", "Kahataruppa", "Kalugahakandura", "Kalupahana", "Kebillawela", "Kendagolla", "Keselpotha", "Ketawatta", "Kiriwanagama", "Koslanda", "Kuruwitenna", "Kuttiyagolla", "Landewela", "Liyangahawela", "Lunugala", "Lunuwatta", "Madulsima", "Mahiyanganaya", "Makulella", "Malgoda", "Mapakadawewa", "Maspanna", "Maussagolla", "Mawanagama", "Medawela Udukinda", "Meegahakiula", "Metigahatenna", "Mirahawatta", "Miriyabedda", "Nawamedagama", "Nelumgama", "Nikapotha", "Nugatalawa", "Ohiya", "Pahalarathkinda", "Pallekiruwa", "Passara", "Pattiyagedara", "Pelagahatenna", "Perawella", "Pitamaruwa", "Pitapola", "Puhulpola", "Rajagalatenna", "Rathkarawwa", "Ridimaliyadda", "Silmiyapura", "Sirimalgoda", "Siripura", "Sorabora Colony", "Soragune", "Soranathota", "Taldena", "Timbirigaspitiya", "Uduhawara", "Uraniya", "Uva Karandagolla", "Uva Mawelagama", "Uva Tenna", "Uva Tissapura", "Welimada", "Weranketagoda", "Wewatta", "Wineethagama", "Yalagamuwa", "Yalwela", "Addalaichenai", "Ampilanthurai", "Araipattai", "Ayithiyamalai", "Bakiella", "Batticaloa", "Cheddipalayam", "Chenkaladi", "Eravur", "Kaluwanchikudi", "Kaluwankeny", "Kannankudah", "Karadiyanaru", "Kathiraveli", "Kattankudi", "Kiran", "Kirankulam", "Koddaikallar", "Kokkaddicholai", "Kurukkalmadam", "Mandur", "Miravodai", "Murakottanchanai", "Navagirinagar", "Navatkadu", "Oddamavadi", "Palamunai", "Pankudavely", "Periyaporativu", "Periyapullumalai", "Pillaiyaradi", "Punanai", "Thannamunai", "Thettativu", "Thikkodai", "Thirupalugamam", "Unnichchai", "Vakaneri", "Vakarai", "Valaichenai", "Vantharumoolai", "Vellavely", "Akarawita", "Ambalangoda", "Athurugiriya", "Avissawella", "Batawala", "Battaramulla", "Biyagama", "Bope", "Boralesgamuwa", "Colombo 8", "Dedigamuwa", "Dehiwala", "Deltara", "Habarakada", "Hanwella", "Hiripitya", "Hokandara", "Homagama", "Horagala", "Kaduwela", "Kaluaggala", "Kapugoda", "Kehelwatta", "Kiriwattuduwa", "Kolonnawa", "Kosgama", "Madapatha", "Maharagama", "Malabe", "Moratuwa", "Mount Lavinia", "Mullegama", "Napawela", "Nugegoda", "Padukka", "Pannipitiya", "Piliyandala", "Pitipana Homagama", "Polgasowita", "Pugoda", "Ranala", "Siddamulla", "Siyambalagoda", "Sri Jayawardenepura", "Talawatugoda", "Tummodara", "Waga", "Colombo 6", "Agaliya", "Ahangama", "Ahungalla", "Akmeemana", "Alawatugoda", "Aluthwala", "Ampegama", "Amugoda", "Anangoda", "Angulugaha", "Ankokkawala", "Aselapura", "Baddegama", "Balapitiya", "Banagala", "Batapola", "Bentota", "Boossa", "Dellawa", "Dikkumbura", "Dodanduwa", "Ella Tanabaddegama", "Elpitiya", "Galle", "Ginimellagaha", "Gintota", "Godahena", "Gonamulla Junction", "Gonapinuwala", "Habaraduwa", "Haburugala", "Hikkaduwa", "Hiniduma", "Hiyare", "Kahaduwa", "Kahawa", "Karagoda", "Karandeniya", "Kosgoda", "Kottawagama", "Kottegoda", "Kuleegoda", "Magedara", "Mahawela Sinhapura", "Mapalagama", "Mapalagama Central", "Mattaka", "Meda-Keembiya", "Meetiyagoda", "Nagoda", "Nakiyadeniya", "Nawandagala", "Neluwa", "Nindana", "Pahala Millawa", "Panangala", "Pannimulla Panagoda", "Parana Thanayamgoda", "Patana", "Pitigala", "Poddala", "Polgampola", "Porawagama", "Rantotuwila", "Talagampola", "Talgaspe", "Talpe", "Tawalama", "Tiranagama", "Udalamatta", "Udugama", "Uluvitike", "Unawatuna", "Unawitiya", "Uragaha", "Uragasmanhandiya", "Wakwella", "Walahanduwa", "Wanchawela", "Wanduramba", "Warukandeniya", "Watugedara", "Weihena", "Welikanda", "Wilanagama", "Yakkalamulla", "Yatalamatta", "Akaragama", "Ambagaspitiya", "Ambepussa", "Andiambalama", "Attanagalla", "Badalgama", "Banduragoda", "Batuwatta", "Bemmulla", "Biyagama IPZ", "Bokalagama", "Bollete (WP)", "Bopagama", "Buthpitiya", "Dagonna", "Danowita", "Debahera", "Dekatana", "Delgoda", "Delwagura", "Demalagama", "Demanhandiya", "Dewalapola", "Divulapitiya", "Divuldeniya", "Dompe", "Dunagaha", "Ekala", "Ellakkala", "Essella", "Galedanda", "Gampaha", "Ganemulla", "Giriulla", "Gonawala", "Halpe", "Hapugastenna", "Heiyanthuduwa", "Hinatiyana Madawala", "Hiswella", "Horampella", "Hunumulla", "Hunupola", "Ihala Madampella", "Imbulgoda", "Ja-Ela", "Kadawatha", "Kahatowita", "Kalagedihena", "Kaleliya", "Kandana", "Katana", "Katudeniya", "Katunayake", "Katunayake Air Force Camp", "Katunayake(FTZ)", "Katuwellegama", "Kelaniya", "Kimbulapitiya", "Kirindiwela", "Kitalawalana", "Kochchikade", "Kotadeniyawa", "Kotugoda", "Kumbaloluwa", "Loluwagoda", "Mabodale", "Madelgamuwa", "Makewita", "Makola", "Malwana", "Mandawala", "Marandagahamula", "Mellawagedara", "Minuwangoda", "Mirigama", "Miriswatta", "Mithirigala", "Muddaragama", "Mudungoda", "Mulleriyawa New Town", "Naranwala", "Nawana", "Nedungamuwa", "Negombo", "Nikadalupotha", "Nikahetikanda", "Nittambuwa", "Niwandama", "Opatha", "Pamunugama", "Pamunuwatta", "Panawala", "Pasyala", "Peliyagoda", "Pepiliyawala", "Pethiyagoda", "Polpithimukulana", "Puwakpitiya", "Radawadunna", "Radawana", "Raddolugama", "Ragama", "Ruggahawila", "Seeduwa", "Siyambalape", "Talahena", "Thambagalla", "Thimbirigaskatuwa", "Tittapattara", "Udathuthiripitiya", "Udugampola", "Uggalboda", "Urapola", "Uswetakeiyawa", "Veyangoda", "Walgammulla", "Walpita", "Walpola (WP)", "Wathurugama", "Watinapaha", "Wattala", "Weboda", "Wegowwa", "Weweldeniya", "Yakkala", "Yatiyana", "Ambalantota", "Angunakolapelessa", "Angunakolawewa", "Bandagiriya Colony", "Barawakumbuka", "Beliatta", "Beragama", "Beralihela", "Bundala", "Ellagala", "Gangulandeniya", "Getamanna", "Goda Koggalla", "Gonagamuwa Uduwila", "Gonnoruwa", "Hakuruwela", "Hambantota", "Handugala", "Hungama", "Ihala Beligalla", "Iththa Demaliya", "Julampitiya", "Kahandamodara", "Kariyamaditta", "Katuwana", "Kawantissapura", "Kirama", "Kirinda", "Lunama", "Lunugamwehera", "Magama", "Mahagalwewa", "Mamadala", "Medamulana", "Middeniya", "Meegahajandura", "Modarawana", "Mulkirigala", "Nakulugamuwa", "Netolpitiya", "Nihiluwa", "Padawkema", "Pahala Andarawewa", "Rammalawarapitiya", "Ranakeliya", "Ranmuduwewa", "Ranna", "Ratmalwala", "Ruhunu Ridiyagama", "Sooriyawewa Town", "Tangalla", "Tissamaharama", "Uda Gomadiya", "Udamattala", "Uswewa", "Vitharandeniya", "Walasmulla", "Weeraketiya", "Weerawila", "Weerawila NewTown", "Wekandawela", "Weligatta", "Yatigala", "Jaffna", "Agalawatta", "Alubomulla", "Anguruwatota", "Atale", "Baduraliya", "Bandaragama", "Batugampola", "Bellana", "Beruwala", "Bolossagama", "Bombuwala", "Boralugoda", "Bulathsinhala", "Danawala Thiniyawala", "Delmella", "Dharga Town", "Diwalakada", "Dodangoda", "Dombagoda", "Ethkandura", "Galpatha", "Gamagoda", "Gonagalpura", "Gonapola Junction", "Govinna", "Gurulubadda", "Halkandawila", "Haltota", "Halvitigala Colony", "Halwala", "Halwatura", "Handapangoda", "Hedigalla Colony", "Henegama", "Hettimulla", "Horana", "Ittapana", "Kahawala", "Kalawila Kiranthidiya", "Kalutara", "Kananwila", "Kandanagama", "Kelinkanda", "Kitulgoda", "Koholana", "Kuda Uduwa", "Labbala", "Ihalahewessa", "Induruwa", "Ingiriya", "Maggona", "Mahagama", "Mahakalupahana", "Maharangalla", "Malgalla Talangalla", "Matugama", "Meegahatenna", "Meegama", "Meegoda", "Millaniya", "Millewa", "Miwanapalana", "Molkawa", "Morapitiya", "Morontuduwa", "Nawattuduwa", "Neboda", "Padagoda", "Pahalahewessa", "Paiyagala", "Panadura", "Pannala", "Paragastota", "Paragoda", "Paraigama", "Pelanda", "Pelawatta", "Pimbura", "Pitagaldeniya", "Pokunuwita", "Poruwedanda", "Ratmale", "Remunagoda", "Talgaswela", "Tebuwana", "Uduwara", "Utumgama", "Veyangalla", "Wadduwa", "Walagedara", "Walallawita", "Waskaduwa", "Welipenna", "Weliveriya", "Welmilla Junction", "Weragala", "Yagirala", "Yatadolawatta", "Yatawara Junction", "Aludeniya", "Ambagahapelessa", "Ambagamuwa Udabulathgama", "Ambatenna", "Ampitiya", "Ankumbura", "Atabage", "Balana", "Bambaragahaela", "Batagolladeniya", "Batugoda", "Batumulla", "Bawlana", "Bopana", "Danture", "Dedunupitiya", "Dekinda", "Deltota", "Divulankadawala", "Dolapihilla", "Dolosbage", "Dunuwila", "Etulgama", "Galaboda", "Galagedara", "Galaha", "Galhinna", "Gampola", "Gelioya", "Godamunna", "Gomagoda", "Gonagantenna", "Gonawalapatana", "Gunnepana", "Gurudeniya", "Hakmana", "Handaganawa", "Handawalapitiya", "Handessa", "Hanguranketha", "Harangalagama", "Hataraliyadda", "Hindagala", "Hondiyadeniya", "Hunnasgiriya", "Inguruwatta", "Jambugahapitiya", "Kadugannawa", "Kahataliyadda", "Kalugala", "Kandy", "Kapuliyadde", "Katugastota", "Katukitula", "Kelanigama", "Kengalla", "Ketaboola", "Ketakumbura", "Kobonila", "Kolabissa", "Kolongoda", "Kulugammana", "Kumbukkandura", "Kumburegama", "Kundasale", "Leemagahakotuwa", "Ihala Kobbekaduwa", "Lunugama", "Lunuketiya Maditta", "Madawala Bazaar", "Madawalalanda", "Madugalla", "Madulkele", "Mahadoraliyadda", "Mahamedagama", "Mahanagapura", "Mailapitiya", "Makkanigama", "Makuldeniya", "Mangalagama", "Mapakanda", "Marassana", "Marymount Colony", "Mawatura", "Medamahanuwara", "Medawala Harispattuwa", "Meetalawa", "Megoda Kalugamuwa", "Menikdiwela", "Menikhinna", "Mimure", "Minigamuwa", "Minipe", "Moragahapallama", "Murutalawa", "Muruthagahamulla", "Nanuoya", "Naranpanawa", "Narawelpita", "Nawalapitiya", "Nawathispane", "Nillambe", "Nugaliyadda", "Ovilikanda", "Pallekotuwa", "Panwilatenna", "Paradeka", "Pasbage", "Pattitalawa", "Peradeniya", "Pilimatalawa", "Poholiyadda", "Pubbiliya", "Pupuressa", "Pussellawa", "Putuhapuwa", "Rajawella", "Rambukpitiya", "Rambukwella", "Rangala", "Rantembe", "Sangarajapura", "Senarathwela", "Talatuoya", "Teldeniya", "Tennekumbura", "Uda Peradeniya", "Udahentenna", "Udatalawinna", "Udispattuwa", "Ududumbara", "Uduwahinna", "Uduwela", "Ulapane", "Unuwinna", "Velamboda", "Watagoda", "Watagoda Harispattuwa", "Wattappola", "Weligampola", "Wendaruwa", "Weragantota", "Werapitya", "Werellagama", "Wettawa", "Yahalatenna", "Yatihalagala", "Alawala", "Alawatura", "Alawwa", "Algama", "Alutnuwara", "Ambalakanda", "Ambulugala", "Amitirigala", "Ampagala", "Anhandiya", "Anhettigama", "Aranayaka", "Aruggammana", "Batuwita", "Beligala(Sab)", "Belihuloya", "Berannawa", "Bopitiya", "Bopitiya (SAB)", "Boralankada", "Bossella", "Bulathkohupitiya", "Damunupola", "Debathgama", "Dedugala", "Deewala Pallegama", "Dehiowita", "Deldeniya", "Deloluwa", "Deraniyagala", "Dewalegama", "Dewanagala", "Dombemada", "Dorawaka", "Dunumala", "Galapitamada", "Galatara", "Galigamuwa Town", "Gallella", "Galpatha(Sab)", "Gantuna", "Getahetta", "Godagampola", "Gonagala", "Hakahinna", "Hakbellawaka", "Halloluwa", "Hedunuwewa", "Hemmatagama", "Hewadiwela", "Hingula", "Hinguralakanda", "Hingurana", "Hiriwadunna", "Ihala Walpola", "Ihalagama", "Imbulana", "Imbulgasdeniya", "Kabagamuwa", "Kahapathwala", "Kandaketya", "Kannattota", "Karagahinna", "Kegalle", "Kehelpannala", "Ketawala Leula", "Kitulgala", "Kondeniya", "Kotiyakumbura", "Lewangama", "Mahabage", "Makehelwala", "Malalpola", "Maldeniya", "Maliboda", "Maliyadda", "Malmaduwa", "Marapana", "Mawanella", "Meetanwala", "Migastenna Sabara", "Miyanawita", "Molagoda", "Morontota", "Narangala", "Narangoda", "Nattarampotha", "Nelundeniya", "Niyadurupola", "Noori", "Pannila", "Pattampitiya", "Pilawala", "Pothukoladeniya", "Puswelitenna", "Rambukkana", "Rilpola", "Rukmale", "Ruwanwella", "Samanalawewa", "Seaforth Colony", "Colombo 2", "Spring Valley", "Talgaspitiya", "Teligama", "Tholangamuwa", "Thotawella", "Udaha Hawupe", "Udapotha", "Uduwa", "Undugoda", "Ussapitiya", "Wahakula", "Waharaka", "Wanaluwewa", "Warakapola", "Watura", "Weeoya", "Wegalla", "Weligalla", "Welihelatenna", "Wewelwatta", "Yatagama", "Yatapana", "Yatiyantota", "Yattogoda", "Kandavalai", "Karachchi", "Kilinochchi", "Pachchilaipalli", "Poonakary", "Akurana", "Alahengama", "Alahitiyawa", "Ambakote", "Ambanpola", "Andiyagala", "Anukkane", "Aragoda", "Ataragalla", "Awulegama", "Balalla", "Bamunukotuwa", "Bandara Koswatta", "Bingiriya", "Bogamulla", "Boraluwewa", "Boyagane", "Bujjomuwa", "Buluwala", "Dadayamtalawa", "Dambadeniya", "Daraluwa", "Deegalla", "Demataluwa", "Demuwatha", "Diddeniya", "Digannewa", "Divullegoda", "Diyasenpura", "Dodangaslanda", "Doluwa", "Doragamuwa", "Doratiyawa", "Dunumadalawa", "Dunuwilapitiya", "Ehetuwewa", "Elibichchiya", "Embogama", "Etungahakotuwa", "Galadivulwewa", "Galgamuwa", "Gallellagama", "Gallewa", "Ganegoda", "Girathalana", "Gokaralla", "Gonawila", "Halmillawewa", "Handungamuwa", "Harankahawa", "Helamada", "Hengamuwa", "Hettipola", "Hewainna", "Hilogama", "Hindagolla", "Hiriyala Lenawa", "Hiruwalpola", "Horambawa", "Hulogedara", "Hulugalla", "Ihala Gomugomuwa", "Ihala Katugampala", "Indulgodakanda", "Ithanawatta", "Kadigawa", "Kalankuttiya", "Kalatuwawa", "Kalugamuwa", "Kanadeniyawala", "Kanattewewa", "Kandegedara", "Karagahagedara", "Karambe", "Katiyawa", "Katupota", "Kawudulla", "Kawuduluwewa Stagell", "Kekunagolla", "Keppitiwalana", "Kimbulwanaoya", "Kirimetiyawa", "Kirindawa", "Kirindigalla", "Kithalawa", "Kitulwala", "Kobeigane", "Kohilagedara", "Konwewa", "Kosdeniya", "Kosgolla", "Kotagala", "Colombo 13", "Kotawehera", "Kudagalgamuwa", "Kudakatnoruwa", "Kuliyapitiya", "Kumaragama", "Kumbukgeta", "Kumbukwewa", "Kuratihena", "Kurunegala", "Ibbagamuwa", "Ihala Kadigamuwa", "Lihiriyagama", "Illagolla", "Ilukhena", "Lonahettiya", "Madahapola", "Madakumburumulla", "Madalagama", "Madawala Ulpotha", "Maduragoda", "Maeliya", "Magulagama", "Maha Ambagaswewa", "Mahagalkadawala", "Mahagirilla", "Mahamukalanyaya", "Mahananneriya", "Mahapallegama", "Maharachchimulla", "Mahatalakolawewa", "Mahawewa", "Maho", "Makulewa", "Makulpotha", "Makulwewa", "Malagane", "Mandapola", "Maspotha", "Mawathagama", "Medirigiriya", "Medivawa", "Meegalawa", "Meegaswewa", "Meewellawa", "Melsiripura", "Metikumbura", "Metiyagane", "Minhettiya", "Minuwangete", "Mirihanagama", "Monnekulama", "Moragane", "Moragollagama", "Morathiha", "Munamaldeniya", "Muruthenge", "Mutugala", "Nabadewa", "Nagollagama", "Nagollagoda", "Nakkawatta", "Narammala", "Nawasenapura", "Nawatalwatta", "Nelliya", "Nikaweratiya", "Nugagolla", "Nugawela", "Padeniya", "Padiwela", "Pahalagiribawa", "Pahamune", "Palagala", "Palapathwela", "Palaviya", "Pallewela", "Palukadawala", "Panadaragama", "Panagamuwa", "Panaliya", "Panapitiya", "Panliyadda", "Pansiyagama", "Parape", "Pathanewatta", "Pattiya Watta", "Perakanatta", "Periyakadneluwa", "Pihimbiya Ratmale", "Pihimbuwa", "Pilessa", "Polgahawela", "Polgolla", "Polpithigama", "Pothuhera", "Pothupitiya", "Pujapitiya", "Rakwana", "Ranorawa", "Rathukohodigala", "Ridibendiella", "Ridigama", "Saliya Asokapura", "Sandalankawa", "Sevanapitiya", "Sirambiadiya", "Sirisetagama", "Siyambalangamuwa", "Siyambalawewa", "Solepura", "Solewewa", "Sunandapura", "Talawattegedara", "Tambutta", "Tennepanguwa", "Thalahitimulla", "Thalakolawewa", "Thalwita", "Tharana Udawela", "Thimbiriyawa", "Tisogama", "Thorayaya", "Tulhiriya", "Tuntota", "Tuttiripitigama", "Udagaldeniya", "Udahingulwala", "Udawatta", "Udubaddawa", "Udumulla", "Uhumiya", "Ulpotha Pallekele", "Ulpothagama", "Usgala Siyabmalangamuwa", "Vijithapura", "Wadakada", "Wadumunnegedara", "Walakumburumulla", "Wannigama", "Wannikudawewa", "Wannilhalagama", "Wannirasnayakapura", "Warawewa", "Wariyapola", "Watareka", "Wattegama", "Watuwatta", "Weerapokuna", "Welawa Juncton", "Welipennagahamulla", "Wellagala", "Wellarawa", "Wellawa", "Welpalla", "Wennoruwa", "Weuda", "Wewagama", "Wilgamuwa", "Yakwila", "Yatigaloluwa", "Mannar", "Puthukudiyiruppu", "Akuramboda", "Alawatuwala", "Alwatta", "Ambana", "Aralaganwila", "Ataragallewa", "Bambaragaswewa", "Barawardhana Oya", "Beligamuwa", "Damana", "Dambulla", "Damminna", "Dankanda", "Delwite", "Devagiriya", "Dewahuwa", "Divuldamana", "Dullewa", "Dunkolawatta", "Elkaduwa", "Erawula Junction", "Etanawala", "Galewela", "Galoya Junction", "Gammaduwa", "Gangala Puwakpitiya", "Hasalaka", "Hattota Amuna", "Imbulgolla", "Inamaluwa", "Iriyagolla", "Kaikawala", "Kalundawa", "Kandalama", "Kavudupelella", "Kibissa", "Kiwula", "Kongahawela", "Laggala Pallegama", "Leliambe", "Lenadora", "Ihala Halmillewa", "Illukkumbura", "Madipola", "Mahawela", "Mananwatta", "Maraka", "Matale", "Melipitiya", "Metihakka", "Millawana", "Muwandeniya", "Nalanda", "Naula", "Opalgala", "Pallepola", "Pimburattewa", "Pulastigama", "Ranamuregama", "Rattota", "Selagama", "Sigiriya", "Sinhagama", "Sungavila", "Talagoda Junction", "Talakiriyagama", "Tamankaduwa", "Udasgiriya", "Udatenna", "Ukuwela", "Wahacotte", "Walawela", "Wehigala", "Welangahawatte", "Wewalawewa", "Yatawatta", "Akuressa", "Alapaladeniya", "Aparekka", "Athuraliya", "Bengamuwa", "Bopagoda", "Dampahala", "Deegala Lenama", "Deiyandara", "Denagama", "Denipitiya", "Deniyaya", "Derangala", "Devinuwara (Dondra)", "Dikwella", "Diyagaha", "Diyalape", "Gandara", "Godapitiya", "Gomilamawarala", "Hawpe", "Horapawita", "Kalubowitiyana", "Kamburugamuwa", "Kamburupitiya", "Karagoda Uyangoda", "Karaputugala", "Karatota", "Kekanadura", "Kiriweldola", "Kiriwelkele", "Kolawenigama", "Kotapola", "Lankagama", "Makandura", "Maliduwa", "Maramba", "Matara", "Mediripitiya", "Miella", "Mirissa", "Morawaka", "Mulatiyana Junction", "Nadugala", "Naimana", "Palatuwa", "Parapamulla", "Pasgoda", "Penetiyana", "Pitabeddara", "Puhulwella", "Radawela", "Ransegoda", "Rotumba", "Sultanagoda", "Telijjawila", "Thihagoda", "Urubokka", "Urugamuwa", "Urumutta", "Viharahena", "Walakanda", "Walasgala", "Waralla", "Weligama", "Wilpita", "Yatiyana", "Ayiwela", "Badalkumbura", "Baduluwela", "Bakinigahawela", "Balaharuwa", "Bibile", "Buddama", "Buttala", "Dambagalla", "Diyakobala", "Dombagahawela", "Ethimalewewa", "Ettiliwewa", "Galabedda", "Gamewela", "Hambegamuwa", "Hingurukaduwa", "Hulandawa", "Inginiyagala", "Kandaudapanguwa", "Kandawinna", "Kataragama", "Kotagama", "Kotamuduna", "Kotawehera Mankada", "Kudawewa", "Kumbukkana", "Marawa", "Mariarawa", "Medagana", "Medawelagama", "Miyanakandura", "Monaragala", "Moretuwegama", "Nakkala", "Namunukula", "Nannapurawa", "Nelliyadda", "Nilgala", "Obbegoda", "Okkampitiya", "Pangura", "Pitakumbura", "Randeniya", "Ruwalwela", "Sella Kataragama", "Siyambalagune", "Siyambalanduwa", "Suriara", "Thanamalwila", "Uva Gangodagama", "Uva Kudaoya", "Uva Pelwatta", "Warunagama", "Wedikumbura", "Weherayaya Handapanagala", "Wellawaya", "Wilaoya", "Yudaganawa", "Mullativu", "Agarapathana", "Ambatalawa", "Ambewela", "Bogawantalawa", "Bopattalawa", "Dagampitiya", "Dayagama Bazaar", "Dikoya", "Doragala", "Dunukedeniya", "Egodawela", "Ekiriya", "Elamulla", "Ginigathena", "Gonakele", "Haggala", "Halgranoya", "Hangarapitiya", "Hapugasthalawa", "Harasbedda", "Hatton", "Hewaheta", "Hitigegama", "Jangulla", "Kalaganwatta", "Kandapola", "Karandagolla", "Keerthi Bandarapura", "Kiribathkumbura", "Kotiyagala", "Kotmale", "Kottellena", "Kumbalgamuwa", "Kumbukwela", "Kurupanawela", "Labukele", "Laxapana", "Lindula", "Madulla", "Mandaram Nuwara", "Maskeliya", "Maswela", "Maturata", "Mipanawa", "Mipilimana", "Morahenagama", "Munwatta", "Nayapana Janapadaya", "Nildandahinna", "Nissanka Uyana", "Norwood", "Nuwara Eliya", "Padiyapelella", "Pallebowala", "Panvila", "Pitawala", "Pundaluoya", "Ramboda", "Rikillagaskada", "Rozella", "Rupaha", "Ruwaneliya", "Santhipura", "Talawakele", "Tawalantenna", "Teripeha", "Udamadura", "Udapussallawa", "Uva Deegalla", "Uva Uduwara", "Uvaparanagama", "Walapane", "Watawala", "Widulipura", "Wijebahukanda", "Attanakadawala", "Bakamuna", "Diyabeduma", "Elahera", "Giritale", "Hingurakdamana", "Hingurakgoda", "Jayanthipura", "Kalingaela", "Lakshauyana", "Mankemi", "Minneriya", "Onegama", "Orubendi Siyambalawa", "Palugasdamana", "Panichankemi", "Polonnaruwa", "Talpotha", "Tambala", "Unagalavehera", "Wijayabapura", "Adippala", "Alutgama", "Alutwewa", "Ambakandawila", "Anamaduwa", "Andigama", "Angunawila", "Attawilluwa", "Bangadeniya", "Baranankattuwa", "Battuluoya", "Bujjampola", "Chilaw", "Dalukana", "Dankotuwa", "Dewagala", "Dummalasuriya", "Dunkannawa", "Eluwankulama", "Ettale", "Galamuna", "Galmuruwa", "Hansayapalama", "Ihala Kottaramulla", "Ilippadeniya", "Inginimitiya", "Ismailpuram", "Jayasiripura", "Kakkapalliya", "Kalkudah", "Kalladiya", "Kandakuliya", "Karathivu", "Karawitagara", "Karuwalagaswewa", "Katuneriya", "Koswatta", "Kottantivu", "Kottapitiya", "Kottukachchiya", "Kumarakattuwa", "Kurinjanpitiya", "Kuruketiyawa", "Lunuwila", "Madampe", "Madurankuliya", "Mahakumbukkadawala", "Mahauswewa", "Mampitiya", "Mampuri", "Mangalaeliya", "Marawila", "Mudalakkuliya", "Mugunuwatawana", "Mukkutoduwawa", "Mundel", "Muttibendiwila", "Nainamadama", "Nalladarankattuwa", "Nattandiya", "Nawagattegama", "Nelumwewa", "Norachcholai", "Pallama", "Palliwasalturai", "Panirendawa", "Parakramasamudraya", "Pothuwatawana", "Puttalam", "Puttalam Cement Factory", "Rajakadaluwa", "Saliyawewa Junction", "Serukele", "Siyambalagashene", "Tabbowa", "Talawila Church", "Toduwawa", "Udappuwa", "Uridyawa", "Vanathawilluwa", "Waikkal", "Watugahamulla", "Wennappuwa", "Wijeyakatupotha", "Wilpotha", "Yodaela", "Yogiyana", "Akarella", "Amunumulla", "Atakalanpanna", "Ayagama", "Balangoda", "Batatota", "Beralapanathara", "Bogahakumbura", "Bolthumbe", "Bomluwageaina", "Bowalagama", "Bulutota", "Dambuluwana", "Daugala", "Dela", "Delwala", "Dodampe", "Doloswalakanda", "Dumbara Manana", "Eheliyagoda", "Ekamutugama", "Elapatha", "Ellagawa", "Ellaulla", "Ellawala", "Embilipitiya", "Eratna", "Erepola", "Gabbela", "Gangeyaya", "Gawaragiriya", "Gillimale", "Godakawela", "Gurubewilagama", "Halwinna", "Handagiriya", "Hatangala", "Hatarabage", "Hewanakumbura", "Hidellana", "Hiramadagama", "Horewelagoda", "Ittakanda", "Kahangama", "Kahawatta", "Kalawana", "Kaltota", "Kalubululanda", "Kananke Bazaar", "Kandepuhulpola", "Karandana", "Karangoda", "Kella Junction", "Keppetipola", "Kiriella", "Kiriibbanwewa", "Kolambage Ara", "Kolombugama", "Kolonna", "Kudawa", "Kuruwita", "Lellopitiya", "Imaduwa", "Imbulpe", "Mahagama Colony", "Mahawalatenna", "Makandura", "Malwala Junction", "Malwatta", "Matuwagalagama", "Medagalature", "Meddekanda", "Minipura Dumbara", "Mitipola", "Moragala Kirillapone", "Morahela", "Mulendiyawala", "Mulgama", "Nawalakanda", "Nawinnapinnakanda", "Niralagama", "Nivitigala", "Omalpe", "Opanayaka", "Padalangala", "Pallebedda", "Pallekanda", "Pambagolla", "Panamura", "Panapola", "Paragala", "Parakaduwa", "Pebotuwa", "Pelmadulla", "Pinnawala", "Pothdeniya", "Rajawaka", "Ranwala", "Rassagala", "Rathgama", "Ratna Hangamuwa", "Ratnapura", "Sewanagala", "Sri Palabaddala", "Sudagala", "Thalakolahinna", "Thanjantenna", "Theppanawa", "Thunkama", "Udakarawita", "Udaniriella", "Udawalawe", "Ullinduwawa", "Veddagala", "Vijeriya", "Waleboda", "Watapotha", "Waturawa", "Weligepola", "Welipathayaya", "Wikiliya", "Agbopura", "Buckmigama", "China Bay", "Dehiwatte", "Echchilampattai", "Galmetiyawa", "Gomarankadawala", "Kaddaiparichchan", "Kallar", "Kanniya", "Kantalai", "Kantalai Sugar Factory", "Kiliveddy", "Kinniya", "Kuchchaveli", "Kumburupiddy", "Kurinchakeny", "Lankapatuna", "Mahadivulwewa", "Maharugiramam", "Mallikativu", "Mawadichenai", "Mullipothana", "Mutur", "Neelapola", "Nilaveli", "Pankulam", "Pulmoddai", "Rottawewa", "Sampaltivu", "Sampoor", "Serunuwara", "Seruwila", "Sirajnagar", "Somapura", "Tampalakamam", "Thuraineelavanai", "Tiriyayi", "Toppur", "Trincomalee", "Wanela", "Vavuniya", "Colombo 1", "Colombo 3", "Colombo 4", "Colombo 5", "Colombo 7", "Colombo 9", "Colombo 10", "Colombo 11", "Colombo 12", "Colombo 14"};
                    final String[] district = new String[]{"Ampara", "Anuradhapura", "Badulla", "Batticaloa", "Colombo", "Galle", "Gampaha", "Hambantota", "Jaffna", "Kalutara", "Kandy", "Kegalle", "Kilinochchi", "Kurunegala", "Mannar", "Matale", "Matara", "Monaragala", "Mullaitivu", "Nuwara Eliya", "Polonnaruwa", "Puttalam", "Ratnapura", "Trincomalee", "Vavuniya"};
                    final String[] province = new String[]{"Western", "Central", "Southern", "North Western", "Sabaragamuwa", "Eastern", "Uva", "North Central", "Northern"};

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateUIWithAddress(city, district, province);
                        }
                    });
                }
            }).start();


            fragment.findViewById(R.id.profile_name_id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int i = (otherDetails1.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;

                    Transition customTransition = new CustomTransition();
                    TransitionManager.beginDelayedTransition(layout1, customTransition);
                    otherDetails1.setVisibility(i);

                }
            });

            otherDetails2 = fragment.findViewById(R.id.visiblelayout2);
            layout2 = fragment.findViewById(R.id.layout2);
            layout2.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

            fragment.findViewById(R.id.address_name_id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int i = (otherDetails2.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;

                    Transition customTransition2 = new CustomTransition();
                    TransitionManager.beginDelayedTransition(layout2, customTransition2);
                    otherDetails2.setVisibility(i);

                }
            });


            Service apiService = RetrofitClient.getApiService();

            ProfileUpdateDTO profileUpdate = new ProfileUpdateDTO();
            profileUpdate.setEmail(email);

            Call<Map<String, String>> userUpdateRequest = apiService.profileupdate(profileUpdate);
            userUpdateRequest.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> responseMSG = response.body();

                        if (responseMSG.get("response").equals("EmailExists")) {
                            if (responseMSG.get("m_msg").equals("mobile_null")) {
                                mobileTextView.setText("");
                            } else if (responseMSG.get("m_msg").equals("mobile_done")) {
                                mobileTextView.setText(responseMSG.get("mobile"));
                            }
                        } else {
                            mobileTextView.setText("");
                        }


                    } else {
                        mobileTextView.setText("");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Log.e(TAG, "Failed to make the network request: " + t.getMessage());

                }
            });

            fragment.findViewById(R.id.addLocation).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame_layouts,new MapFragment());
                    transaction.commit();
                }
            });

            fragment.findViewById(R.id.update_profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String updatename = String.valueOf(nameTextView.getText());
                    String updateemail = (String) emailTextView.getText();
                    String updatemobile = String.valueOf(mobileTextView.getText());

                    if (updatename == null || updatename.trim().isEmpty()) {
                        errorName = "Please Enter Your Name";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                    } else if (updatemobile == null || updatemobile.trim().isEmpty()) {
                        errorName = "Please Enter Your Mobile Number";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                    } else if (!updatemobile.trim().matches("\\d{10}")) {
                        errorName = "A valid mobile number is only a 10-digit number";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                    } else if (!updatemobile.trim().matches("^07[1-9]\\d{7}$")) {
                        errorName = "Please input a valid Mobile Number";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                    } else {

                        customLoading.show();

                        Service apiService = RetrofitClient.getApiService();

                        ProfileUpdateDTO profileUpdateDTO = new ProfileUpdateDTO();
                        profileUpdateDTO.setName(updatename);
                        profileUpdateDTO.setEmail(updateemail);
                        profileUpdateDTO.setMobile(updatemobile);

                        Call<Map<String, String>> updateUserRequest = apiService.profileUserupdate(profileUpdateDTO);
                        updateUserRequest.enqueue(new Callback<Map<String, String>>() {
                            @Override
                            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                                Map<String, String> responseMSG = response.body();


                                if (responseMSG.get("response").equals("EmailExists")) {
                                    if (responseMSG.get("results").equals("Success")) {

                                        nameTextView.setText(updatename);
                                        mobileTextView.setText(updatemobile);

                                        SharedPreferences preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor edit = preferences.edit();

                                        edit.putString("NAME", updatename);
                                        edit.apply();

                                        customLoading.dismiss();

                                        successtitle = "Successful!";
                                        successName = "Your Update has been successful";
                                        new ProfileFragment.SuccessDialog().show(getActivity().getSupportFragmentManager(), "Success");

                                    } else {
                                        errorName = response.message();
                                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                                    }
                                } else {
                                    errorName = response.message();
                                    new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                                }
                            }

                            @Override
                            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                                customLoading.dismiss();
                                Log.e(TAG, "Failed to make the network request: " + t.getMessage());
                            }
                        });
                    }
                }
            });


            fragment.findViewById(R.id.update_address).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FragmentActivity activity = getActivity();

                    String city = address_city.getText().toString();
                    String district = address_district.getText().toString();
                    String province = address_province.getText().toString();
                    String ad1 = address_line1.getText().toString();
                    String ad2 = address_line2.getText().toString();
                    String postal_code = address_postal_code.getText().toString();


                    if (ad1 == null || ad1.trim().isEmpty()) {
                        errorName = "Please Enter Your Address Line 1";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                    } else if (ad2 == null || ad2.trim().isEmpty()) {
                        errorName = "Please Enter Your Address Line 2";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                    } else if (city == null || city.trim().isEmpty()) {
                        errorName = "Please Enter Your City";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                    } else if (district == null || district.trim().isEmpty()) {
                        errorName = "Please Enter Your District";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                    } else if (province == null || province.trim().isEmpty()) {
                        errorName = "Please Enter Your Province";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                    } else if (postal_code == null || postal_code.trim().isEmpty()) {
                        errorName = "Please Enter Your Postal Code";
                        new ProfileFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                    } else {

                        String user_email = emailTextView.getText().toString();
                        UserProfile userProfile = new UserProfile(ad1, ad2, city, district, province, postal_code);


                        customLoading.show();

                        DocumentReference userDocument = fireStore.collection("User").document(user_email);

                        userDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();

                                    fireStore.collection("User").document(user_email).set(userProfile).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            customLoading.dismiss();
                                            loadAddress();
                                            successtitle = "Successful!";
                                            successName = "Your Address has been Added Successfully";
                                            if (activity != null) {
                                                new ProfileFragment.SuccessDialog().show(getActivity().getSupportFragmentManager(), "Success");
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            customLoading.dismiss();
                                            errorName = "Your Address did not Added";
                                            if (activity != null) {
                                                new ProfileFragment.ErrorDialog().show(activity.getSupportFragmentManager(), "Error");
                                            }
                                        }
                                    });

                                } else {
                                    customLoading.dismiss();
                                    errorName = task.getException().getMessage();
                                    if (activity != null) {
                                        new ProfileFragment.ErrorDialog().show(activity.getSupportFragmentManager(), "Error");
                                    }
                                }
                            }
                        });

                    }
                }
            });

            fragment.findViewById(R.id.uploadImagebtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
                }
            });

            fragment.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Logout.logout(getActivity());

                }
            });



        }

    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == Activity.RESULT_OK) {
                imagePath = o.getData().getData();
                Picasso.get().load(imagePath).resize(200, 200).centerCrop().into(imageView);

                String user_email = emailTextView.getText().toString();

                customLoading.show();

                if (imagePath != null) {
                    StorageReference reference = storage.getReference("user_images")
                            .child(user_email);

                    reference.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            customLoading.dismiss();


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            customLoading.dismiss();

                        }
                    });
                }

            }
        }
    });

    public static class ErrorDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.error_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(errorName);

            Button okButton = customView.findViewById(R.id.success_dialog_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }

    public static class SuccessDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.success_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(successName);

            TextView messageTextView2 = customView.findViewById(R.id.success_dialog_title);
            messageTextView2.setText(successtitle);

            Button okButton = customView.findViewById(R.id.success_dialog_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }

    private void updateUIWithAddress(String[] city, String[] district, String[] province) {
        if (getActivity() != null) {
            address_city = getView().findViewById(R.id.address_city);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, city);
            address_city.setAdapter(adapter);

            address_district = getView().findViewById(R.id.address_district);
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, district);
            address_district.setAdapter(adapter2);

            address_province = getView().findViewById(R.id.address_province);
            ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, province);
            address_province.setAdapter(adapter3);
        }
    }

    private void loadAddress() {

        String user_email = emailTextView.getText().toString();
        if (listenerRegistration == null) {
            listenerRegistration = fireStore.collection("User").document(user_email).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value.exists()) {
                        address_line1.setText(value.getString("ad1"));
                        address_line2.setText(value.getString("ad2"));
                        address_city.setText(value.getString("city"));
                        address_district.setText(value.getString("district"));
                        address_province.setText(value.getString("province"));
                        address_postal_code.setText(value.getString("postalCode"));
                    } else {
                        Log.i(TAG, "Document does not exist");
                    }
                }
            });
        }
    }

}
