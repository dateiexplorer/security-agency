for component in "shift" "shift_cracker" "rsa" "rsa_cracker"; do
     jarsigner -keystore keystore.jks -storepass msa-dhbw components/$component/build/libs/$component.jar server
done;


