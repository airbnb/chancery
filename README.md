# Welcome to Chancery #

Chancery hears when you push to Github, and allows you to…

- **Deploy when github is down!**

  Chancery can send gzip'ped tarballs of your references of choice to an S3 bucket.

- **Keep track of your branches history!** Even when Pierre force-pushed to `master`!

  A `reflog` for Github, more or less.

*Gratefully based on Java 7, Dropwizard, AWS SDK, MVEL2, Joda Time and lombok.*

**Table of Contents**

- [What it does](#what-it-does)
- [Set it up (one-time operation)](#set-it-up-one-time-operation)
    - [Prepare credentials](#prepare-credentials)
        - [Github](#github)
        - [AWS (only if you want S3 tarballs)](#aws-only-if-you-want-s3-tarballs)
    - [Build it](#build-it)
    - [Write a configuration file](#write-a-configuration-file)
    - [Run it](#run-it)
    - [Monitor it](#monitor-it)
- [Install a web hook (needed for every repository)](#install-a-web-hook-needed-for-every-repository)
- [Contribute](#contribute)
    - [Local development](#local-development)

## What it does ##

Whenever someone updates a reference (tag, branch, etc.) in a monitored repository,
Github lets Chancery know through a web hook. If Github sent the right secret,
Chancery will do its magic.

You can configure a few backends, each of them have their own configuration.

They usually start by trying to match `$REPO_URI:$REF` against their `refFilter`
regular expression. This provides a limited but simple way to specify which branches
and/or tags of which repositories should be acted upon.

- For S3 tarballs, the web hook message is passed to the `keyTemplate` template.
  This template turns it into an "object key", which you can think of
  as the path of the tarball in your bucket.

  If the update deleted the reference, we delete the corresponding object;
  if the update changed the commit the reference points to, we let Github
  bake us a fresh gzip'ped tarball then put it there.

- For ref logs, the web hook message is passed to the `refTemplate` template,
  and the resulting target reference is created.

**A few more things** to note before we get started:

1. References are always in their canonical form:
   a `v1.0` tag is `refs/tags/v1.0`,
   a `feature/cleartextpasswd` branch is `refs/heads/feature/cleartextpasswd`.

2. The secret mechanism relies on a SHA1 HMAC and allows secure authentication of Github.
   HTTPS is however needed to hide the web hook payload from passive observers,
   and for protection against replay attacks.

3. Templates are all MVEL 2 string templates.
   Please look at their [templating guide](http://mvel.codehaus.org/MVEL+2.0+Templating+Guide)
   for the syntax.

   A lot of information from the hook is usable there; please look at the sources
   of `CallbackPayload` or the Chancery debug logs.

   We added a `timestamp` attribute that matches when Chancery received the callback,
   and Joda Time's `ISODateTimeFormat` class is exposed as `iso`, `DateTimeFormat` as `dtf`.

## Set it up (one-time operation) ##

### Prepare credentials ###

#### Github ####

1. Create an `acme-chancery` github user.

2. Create an oAuth2 token for that user, keep it around:

        $ curl \
            --silent --fail \
            --request POST \
            --user 'acme-chancery:password123' \
            -H 'Content-Type: application/json' \
            --data '{ "scopes": ["repo"], "note": "chancery" }' \
            https://api.github.com/authorizations
        {
          [...]
          "token": "1234567890abcdef1234567890abcdef12345678",
          [...]
        }

3. Give the bot access to the repositories. We suggest using a team.
   "Pull-only" is enough for S3 tarballs, but "Push & Pull" is needed for reflogs.

#### AWS (only if you want S3 tarballs) ####

1. Create an AMI user, keep its Access Key Id and Secret Access Key around.

2. Attach a user policy to it:

        {"Statement":[{
            "Action": ["s3:*"],
            "Effect": "Allow",
            "Resource": [
                "arn:aws:s3:::deployment.acme.com/repos/*",
                "arn:aws:s3:::archive.acme.com/repos/*"
            ]
        }]}

### Build it ###

You'll need Java 7 and a recent version of Maven (only tested with Maven 3).

    $ mvn package; ls -l target/chancery-1.0-SNAPSHOT.jar

### Write a configuration file ###

Dropwizard comes with many options, please refer to their documentation and
[commented example](https://github.com/codahale/dropwizard/blob/master/dropwizard-example/example.yml).

Here is an example for our story:

    # Defaults to 16
    handlerThreads: 32

    # Required
    githubOauth2Token: 1234567890abcdef1234567890abcdef12345678
    # Optional. You get to pick it, so we did:
    githubSecret: 'airbnb <3 github :)'

    # Required for S3 tarballs
    awsAccessKeyID: XXXXXXXXXXXXXXXXXXXX
    awsSecretKey: YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY

    s3Archives:
      - refFilter:   https://github\.com/acme/project-(manhattan|brooklyn):refs/heads/prod/.*
        bucketName:  deployment.acme.com
        keyTemplate: repos/@{repository.name}/@{ref}
      - refFilter:   https://github\.com/acme/.*:refs/(heads/prod|tags)/.*
        bucketName:  archive.acme.com
        keyTemplate: repos/@{repository.name}/@{ref}:@{timestamp}

    refLogs:
      - refFilter:   https://github\.com/acme/.*:refs/(heads|tags)
        refTemplate: 'refs/history/@{ref.substring(5)}/@{dtf.forPattern("yyyy/MM/dd/HH/mm/ss").print(timestamp)}'

    # Github can be very slow.
    githubHttpConfig:
      timeout: 20s
      connectionTimeout: 10s
      maxConnectionsPerRoute: 16
      keepAlive: 60s
      # Github's nginx servers require this (or you'll get 411)
      gzipEnabledForRequests: false
    
    # Log ALL before opening an issue please
    logging:
      console:
        enabled: true
        threshold: ALL

### Run it ###

You'll need Java 7, the `chancery-2.0-SNAPSHOT.jar` überjar and a configuration
file.

    $  java -jar chancery-2.0-SNAPSHOT.jar server /etc/chancery.yml

### Monitor it ###

The various monitoring endpoints are listed on the admin port (8081 by default).

You'll a few basic health checks in `/healthcheck` and
a bunch of metrics in `/metrics?pretty`;
feel free to suggest more.

Please remember that the metrics are JMX-friendly;
the Dropwizard documentation is yet again of great help.

## Install a web hook (needed for every repository) ##

You'll need to create the Github web hooks with your own user, as the
`acme-chancery` doesn't have sufficient rights.

    $ curl \
        --silent --fail \
        --request POST \
        --user 'acme-root:god' \
        -H 'Content-Type: application/json' \
        --data '{
                  "name": "web",
                  "active": true,
                  "config": {
                    "content_type": "json",
                    "secret": "airbnb <3 github :)",
                    "url": "https://chancery.ewr.corp.acme.com/callback"
                  }
                }' \
        https://api.github.com/repos/acme/project-manhattan/hooks


Then verify that the Chancery configuration file, in particular `refPattern`,
matches your particular needs.

## Contribute ##

Open issues, send pull requests, share your love with
[@AirbnbNerds](https://twitter.com/AirbnbNerds) on Twitter!

### Code? ###

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Add yourself to the contributors in `pom.xml`
4. Commit your changes (`git commit -am 'Add some feature'`)
5. Push to the branch (`git push origin my-new-feature`)
6. Create new Pull Request

### Local development ###

Live-testing Github callbacks from your development machine could be a bit painful.
Here is a trick.

- On your publicly-accessible `server.acme.com`, make sure you allow gateway ports:

        Match User johndoe
            GatewayPorts yes

  Restart `sshd` if needed.

- From your laptop, run:

        laptop$ ssh -vNR 9000:localhost:8080 server.acme.com

  Note that the `9000` port needs to be reachable from the outside on
  `server.acme.com`, but you do not need to open your laptop firewall as
  connections will come through the loopback interface.

- On your laptop, fire up a logging HTTP server, check that it receives requests:

        $ python -mSimpleHTTPServer 8080 &
        [1] 32430
        Serving HTTP on 0.0.0.0 port 8080 ...
        $ curl -sf http://server.acme.com:9000/ >/dev/null
        127.0.0.1 - - [29/Apr/2013 02:08:48] "GET / HTTP/1.1" 200 -
        $ kill %%

- You can now ask Github to send callbacks to `http://server.acme.com:9000/`.
