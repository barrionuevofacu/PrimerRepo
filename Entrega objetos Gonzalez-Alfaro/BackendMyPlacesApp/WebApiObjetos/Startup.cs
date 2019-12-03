using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.HttpOverrides;
using Microsoft.AspNetCore.HttpsPolicy;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using Swashbuckle.AspNetCore.Swagger;
using WebApiObjetos.Data;
using WebApiObjetos.Models.Repositories;
using WebApiObjetos.Models.Repositories.Interfaces;
using WebApiObjetos.Properties;
using WebApiObjetos.Services;
using WebApiObjetos.Services.Interfaces;

namespace WebApiObjetos
{
    public class Startup
    {
        public IConfiguration Configuration { get; }

        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }


        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.Configure<CookiePolicyOptions>(options =>
            {
                // This lambda determines whether user consent for non-essential cookies is needed for a given request.
                options.CheckConsentNeeded = context => true;
                options.MinimumSameSitePolicy = SameSiteMode.None;
            });

            services.AddSwaggerGen(c => 
            {
                c.SwaggerDoc("v1", new Info { Title = "Swagger UI",Version= "v1.0" });
            });
            
            services.AddDbContext<ApplicationDBContext>(options => options.UseSqlServer(Configuration.GetConnectionString("MyConnStr")));
            services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme) // aca seteas la autenticación por default que queres que se utilice
            .AddJwtBearer(options =>
            {
                options.SaveToken = true;
                options.TokenValidationParameters = new TokenValidationParameters // esta es la clase que instancias para configurar como se valida el token
                {
                    ClockSkew = TimeSpan.FromMinutes(5),
                    ValidateLifetime = true,
                    IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(Resources.Encription_Key)),
                    //obtenerla de los secrets y cuando genero el token obtenerlo de ahi tambien, no tener las claves en la carpeta resources.
                    ValidateIssuerSigningKey = true,
                    RequireExpirationTime = true,
                    ValidateAudience = true,
                    ValidAudience = Resources.Audience,
                    ValidateIssuer = true,
                    ValidIssuer = Resources.Issuer

                };
                options.Events = new JwtBearerEvents /// con esto si falla la autenticación y la causa era que se vencio el token retorna ese header.
                {
                    OnAuthenticationFailed = context =>
                    {
                        if (context.Exception.GetType() == typeof(SecurityTokenExpiredException))
                        {
                            context.Response.Headers.Add("Token-Expired", "true");
                        }
                        return Task.CompletedTask;
                    }
                };


            }); // podes setear las otras formas de autenticación aca(por ejemplo con cookie)
/*
            services.AddAuthorization(options =>
            {
                options.AddPolicy("Admin", adminoptions =>
                 {
                     adminoptions.RequireAuthenticatedUser().RequireRole("Admin");
                 });

            });
            se puede setear distintos roles como admin y usuario con esto
            */

            
            services.AddCors(options =>
            {
                options.AddPolicy("CorsPolicy",
                    builder => builder.AllowAnyOrigin()
                      .AllowAnyMethod()
                      .AllowAnyHeader()
                      .AllowCredentials()
                .Build());
            });
            //esto acepta requests de cualquier tipo de cualquier página, no es seguro , pero podes cambiar el allow anyOrigin por WithOrigins("nombre origen")

            services.AddScoped<ILocationService, LocationService>();
            services.AddScoped<IImageRepository, ImageRepository>();
            services.AddScoped<IUserService, UserService>();
            services.AddScoped<DbContext, ApplicationDBContext>();
            services.AddScoped<IUserRepository, UserRepository>();
            services.AddScoped<ILocationRepository, LocationRepository>();
            services.AddMvc(options =>
            {
            }).SetCompatibilityVersion(CompatibilityVersion.Version_2_1);

        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IHostingEnvironment env, ILogger<Startup> logger)
        {
            if (env.IsDevelopment())
            {
                DeveloperExceptionPageOptions developerExceptionPageOptions = new DeveloperExceptionPageOptions();
                developerExceptionPageOptions.SourceCodeLineCount = 10; // muestra 10 lineas + y 10 lineas menos de donde se genero la excepcion.
                app.UseDeveloperExceptionPage(); // es mejor ponerlo lo antes posible por si se rompe alguno de los otros pipes
            }
            else
            {
                app.UseExceptionHandler("/Home/Error");
                app.UseHsts();
            }

            app.UseForwardedHeaders(new ForwardedHeadersOptions
            {
                ForwardedHeaders = ForwardedHeaders.XForwardedFor | ForwardedHeaders.XForwardedProto /// esto se usa si existe un load balancer que termina ssl por vos, esto forwardea los headers para que te funcione ssl igual
            });

            //avanzado.
            app.UseHsts(options => options.MaxAge(days : 365).IncludeSubdomains());// cuando alguien se conecta con http el servidor recibe el request y despues lo redirecciona, con esto aplico a todos los subdominios que nisiquiera llegue la conexion http.
            app.UseXXssProtection(options => options.EnabledWithBlockMode());// previene cross site scripting en ciertos browsers
            app.UseXContentTypeOptions();// esto previene ataques en los que los browsers traten data de una página como un diferente tipo del que realmente es
            

            app.UseStaticFiles();
            app.UseCookiePolicy();
            logger.LogInformation("comienzo del Programa"); // loggeo el inicio, imprime por consola.
           

            //app.UseCors(); esto junto con lo otro comentado permiten cors.
            app.UseAuthentication();
           
            app.UseMvc(routes =>
            {
                routes.MapRoute(
                    name: "default",
                    template: "{controller=Home}/{action=Index}/{id?}");
            });


            //para usar swagger
            app.UseSwagger();
            app.UseSwaggerUI(c => 
            {
                c.SwaggerEndpoint("/swagger/v1/swagger.json", "swagger test");
            });
        }
    }
}

