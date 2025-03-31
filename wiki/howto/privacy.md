# Privacy Request --- Finding a User Record by Email

When a privacy request comes in, e.g., from privacy@sap.com, we have to look for a user record usually by e-mail address. For this there is a script ``configuration/finduserbyemail.sh`` which can be used for this purpose:

```
    ./configuration/finduserbyemail.sh axel.uhl@sap.com
```

will emit the records found, prefixed with the DB name where the record was found. Use these results to respond to the requests. The PET process number is ``3584``.